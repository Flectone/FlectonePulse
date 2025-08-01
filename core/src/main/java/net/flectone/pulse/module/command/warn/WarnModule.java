package net.flectone.pulse.module.command.warn;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformServerAdapter;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.DisableSource;
import net.flectone.pulse.constant.MessageType;
import net.flectone.pulse.formatter.ModerationMessageFormatter;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.provider.CommandParserProvider;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.type.tuple.Pair;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@Singleton
public class WarnModule extends AbstractModuleCommand<Localization.Command.Warn> {

    private final Command.Warn command;
    private final Permission.Command.Warn permission;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final CommandParserProvider commandParserProvider;
    private final PlatformServerAdapter platformServerAdapter;
    private final ProxySender proxySender;
    private final Gson gson;

    @Inject
    public WarnModule(FileResolver fileResolver,
                      FPlayerService fPlayerService,
                      ModerationService moderationService,
                      ModerationMessageFormatter moderationMessageFormatter,
                      CommandParserProvider commandParserProvider,
                      PlatformServerAdapter platformServerAdapter,
                      ProxySender proxySender,
                      Gson gson) {
        super(localization -> localization.getCommand().getWarn(), Command::getWarn, fPlayer -> fPlayer.isSetting(FPlayer.Setting.WARN));

        this.command = fileResolver.getCommand().getWarn();
        this.permission = fileResolver.getPermission().getCommand().getWarn();
        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.moderationMessageFormatter = moderationMessageFormatter;
        this.commandParserProvider = commandParserProvider;
        this.platformServerAdapter = platformServerAdapter;
        this.proxySender = proxySender;
        this.gson = gson;
    }

    @Override
    public void onEnable() {
        // if FPlayer.UNKNOWN (all-permissions) fails check (method will return true),
        // a moderation plugin is intercepting this command
        if (checkModulePredicates(FPlayer.UNKNOWN)) return;

        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        String promptReason = addPrompt(1, Localization.Command.Prompt::getReason);
        String promptTime = addPrompt(2, Localization.Command.Prompt::getTime);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission.getName())
                .required(promptPlayer, commandParserProvider.playerParser(command.isSuggestOfflinePlayers()))
                .optional(promptTime + " " + promptReason, commandParserProvider.durationReasonParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableSource.YOU)) return;
        if (checkMute(fPlayer)) return;

        String target = getArgument(commandContext, 0);
        String promptReason = getPrompt(1);
        String promptTime = getPrompt(2);

        Optional<Pair<Long, String>> optionalTime = commandContext.optional(promptTime + " " + promptReason);
        Pair<Long, String> timeReasonPair = optionalTime.orElse(Pair.of(Duration.ofHours(1).toMillis(), null));

        long time = timeReasonPair.first() == -1 ? Duration.ofHours(1).toMillis() : timeReasonPair.first();

        if (time < 1) {
            builder(fPlayer)
                    .format(Localization.Command.Warn::getNullTime)
                    .sendBuilt();
            return;
        }

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Warn::getNullPlayer)
                    .sendBuilt();
            return;
        }

        long databaseTime = time + System.currentTimeMillis();
        String reason = timeReasonPair.second();

        Moderation warn = moderationService.warn(fTarget, databaseTime, reason, fPlayer.getId());
        if (warn == null) return;

        proxySender.send(fTarget, MessageType.SYSTEM_WARN, dataOutputStream -> {});

        builder(fTarget)
                .range(command.getRange())
                .destination(command.getDestination())
                .tag(MessageType.COMMAND_WARN)
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
