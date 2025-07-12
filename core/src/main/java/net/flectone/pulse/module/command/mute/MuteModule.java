package net.flectone.pulse.module.command.mute;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.formatter.ModerationMessageFormatter;
import net.flectone.pulse.util.Pair;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.time.Duration;
import java.util.Optional;
import java.util.function.BiFunction;

@Singleton
public class MuteModule extends AbstractModuleCommand<Localization.Command.Mute> {

    @Getter private final Command.Mute command;
    private final Permission.Command.Mute permission;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final CommandRegistry commandRegistry;
    private final ProxySender proxySender;
    private final Gson gson;

    @Inject
    public MuteModule(FileResolver fileResolver,
                      FPlayerService fPlayerService,
                      ModerationService moderationService,
                      ModerationMessageFormatter moderationMessageFormatter,
                      CommandRegistry commandRegistry,
                      ProxySender proxySender,
                      Gson gson) {
        super(localization -> localization.getCommand().getMute(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.MUTE));

        this.command = fileResolver.getCommand().getMute();
        this.permission = fileResolver.getPermission().getCommand().getMute();
        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.moderationMessageFormatter = moderationMessageFormatter;
        this.commandRegistry = commandRegistry;
        this.proxySender = proxySender;
        this.gson = gson;
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void onEnable() {
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
        if (checkMute(fPlayer)) return;

        String promptReason = getPrompt().getReason();
        String promptTime = getPrompt().getTime();

        Optional<Pair<Long, String>> optionalTime = commandContext.optional(promptTime + " " + promptReason);
        Pair<Long, String> timeReasonPair = optionalTime.orElse(new Pair<>(Duration.ofHours(1).toMillis(), null));

        long time = timeReasonPair.left() == -1 ? Duration.ofHours(1).toMillis() : timeReasonPair.left();

        if (time < 1) {
            builder(fPlayer)
                    .format(Localization.Command.Mute::getNullTime)
                    .sendBuilt();
            return;
        }

        String reason = timeReasonPair.right();

        String promptPlayer = getPrompt().getPlayer();
        String target = commandContext.get(promptPlayer);
        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Mute::getNullPlayer)
                    .sendBuilt();
            return;
        }

        long databaseTime = time + System.currentTimeMillis();

        Moderation mute = moderationService.mute(fTarget, databaseTime, reason, fPlayer.getId());
        if (mute == null) return;

        proxySender.sendMessage(fTarget, MessageTag.SYSTEM_MUTE, dataOutputStream -> {});

        builder(fTarget)
                .range(command.getRange())
                .destination(command.getDestination())
                .tag(MessageTag.COMMAND_MUTE)
                .format(buildFormat(mute))
                .proxy(output -> {
                    output.writeUTF(gson.toJson(fPlayer));
                    output.writeUTF(gson.toJson(mute));
                })
                .integration(s -> moderationMessageFormatter.replacePlaceholders(s, FPlayer.UNKNOWN, mute))
                .sound(getSound())
                .sendBuilt();

        sendForTarget(fPlayer, fTarget, mute);
    }

    public BiFunction<FPlayer, Localization.Command.Mute, String> buildFormat(Moderation mute) {
        return (fReceiver, message) -> moderationMessageFormatter.replacePlaceholders(message.getServer(), fReceiver, mute);
    }

    public void sendForTarget(FEntity fModerator, FPlayer fReceiver, Moderation mute) {
        if (checkModulePredicates(fModerator)) return;

        builder(fReceiver)
                .format(s -> moderationMessageFormatter.replacePlaceholders(s.getPerson(), fReceiver, mute))
                .sound(getSound())
                .sendBuilt();
    }
}
