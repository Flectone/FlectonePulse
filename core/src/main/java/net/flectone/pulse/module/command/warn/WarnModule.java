package net.flectone.pulse.module.command.warn;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.adapter.PlatformServerAdapter;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.formatter.ModerationMessageFormatter;
import net.flectone.pulse.util.Pair;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@Singleton
public class WarnModule extends AbstractModuleCommand<Localization.Command.Warn> {

    @Getter private final Command.Warn command;
    private final Permission.Command.Warn permission;

    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final CommandRegistry commandRegistry;
    private final PlatformServerAdapter platformServerAdapter;
    private final ProxySender proxySender;
    private final Gson gson;

    @Inject
    public WarnModule(FileManager fileManager,
                      FPlayerService fPlayerService,
                      ModerationService moderationService,
                      ModerationMessageFormatter moderationMessageFormatter,
                      CommandRegistry commandRegistry,
                      PlatformServerAdapter platformServerAdapter,
                      ProxySender proxySender,
                      Gson gson) {
        super(localization -> localization.getCommand().getWarn(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.WARN));

        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.moderationMessageFormatter = moderationMessageFormatter;
        this.commandRegistry = commandRegistry;
        this.platformServerAdapter = platformServerAdapter;
        this.proxySender = proxySender;
        this.gson = gson;

        command = fileManager.getCommand().getWarn();
        permission = fileManager.getPermission().getCommand().getWarn();
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void reload() {
        if (checkModulePredicates(FPlayer.UNKNOWN)) return;

        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptPlayer = getPrompt().getPlayer();
        String promptReason = getPrompt().getReason();
        String promptTime = getPrompt().getTime();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .required(promptPlayer, commandRegistry.playerParser(command.isSuggestOfflinePlayers()))
                        .optional(promptTime + " " + promptReason, commandRegistry.durationReasonParser())
                        .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkMute(fPlayer)) return;

        String promptReason = getPrompt().getReason();
        String promptTime = getPrompt().getTime();

        Optional<Pair<Long, String>> optionalTime = commandContext.optional(promptTime + " " + promptReason);
        Pair<Long, String> timeReasonPair = optionalTime.orElse(new Pair<>(Duration.ofHours(1).toMillis(), null));

        long time = timeReasonPair.left() == -1 ? Duration.ofHours(1).toMillis() : timeReasonPair.left();

        if (time < 1) {
            builder(fPlayer)
                    .format(Localization.Command.Warn::getNullTime)
                    .sendBuilt();
            return;
        }

        String reason = timeReasonPair.right();

        String promptPlayer = getPrompt().getPlayer();
        String target = commandContext.get(promptPlayer);

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Warn::getNullPlayer)
                    .sendBuilt();
            return;
        }

        long databaseTime = time + System.currentTimeMillis();

        Moderation warn = moderationService.warn(fTarget, databaseTime, reason, fPlayer.getId());
        if (warn == null) return;

        proxySender.sendMessage(fTarget, MessageTag.SYSTEM_WARN, byteArrayDataOutput -> {});

        builder(fTarget)
                .range(command.getRange())
                .destination(command.getDestination())
                .tag(MessageTag.COMMAND_WARN)
                .format(buildFormat(warn))
                .proxy(output -> {
                    output.writeUTF(gson.toJson(fPlayer));
                    output.writeUTF(gson.toJson(warn));
                })
                .integration(s -> moderationMessageFormatter.replacePlaceholders(s, FPlayer.UNKNOWN, warn))
                .sound(getSound())
                .sendBuilt();

        send(fPlayer, fTarget, warn);

        List<Moderation> warns = moderationService.getValidWarns(fTarget);
        if (warns.isEmpty()) return;

        int countWarns = warns.stream()
                .filter(moderation -> moderation.isValid() && !moderation.isExpired())
                .toList().size();

        String action = command.getActions().get(countWarns);
        if (action == null) return;

        platformServerAdapter.dispatchCommand(action.replace("<target>", fTarget.getName()));
    }

    public BiFunction<FPlayer, Localization.Command.Warn, String> buildFormat(Moderation warn) {
        return (fReceiver, message) -> moderationMessageFormatter.replacePlaceholders(message.getServer(), fReceiver, warn);
    }

    public void send(FEntity fModerator, FPlayer fReceiver, Moderation warn) {
        if (checkModulePredicates(fModerator)) return;

        builder(fReceiver)
                .format(s -> moderationMessageFormatter.replacePlaceholders(s.getPerson(), fReceiver, warn))
                .sound(getSound())
                .sendBuilt();
    }
}
