package net.flectone.pulse.module.command.warn;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.ModerationMetadata;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.type.tuple.Pair;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@Singleton
public class WarnModule extends AbstractModuleCommand<Localization.Command.Warn> {

    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final CommandParserProvider commandParserProvider;
    private final PlatformServerAdapter platformServerAdapter;
    private final ProxySender proxySender;

    @Inject
    public WarnModule(FileResolver fileResolver,
                      FPlayerService fPlayerService,
                      ModerationService moderationService,
                      ModerationMessageFormatter moderationMessageFormatter,
                      CommandParserProvider commandParserProvider,
                      PlatformServerAdapter platformServerAdapter,
                      ProxySender proxySender) {
        super(MessageType.COMMAND_WARN);

        this.fileResolver = fileResolver;
        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.moderationMessageFormatter = moderationMessageFormatter;
        this.commandParserProvider = commandParserProvider;
        this.platformServerAdapter = platformServerAdapter;
        this.proxySender = proxySender;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        String promptReason = addPrompt(1, Localization.Command.Prompt::getReason);
        String promptTime = addPrompt(2, Localization.Command.Prompt::getTime);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().getName())
                .required(promptPlayer, commandParserProvider.playerParser(config().isSuggestOfflinePlayers()))
                .optional(promptTime + " " + promptReason, commandParserProvider.durationReasonParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String target = getArgument(commandContext, 0);
        String promptReason = getPrompt(1);
        String promptTime = getPrompt(2);

        Optional<Pair<Long, String>> optionalTime = commandContext.optional(promptTime + " " + promptReason);
        Pair<Long, String> timeReasonPair = optionalTime.orElse(Pair.of(Duration.ofHours(1).toMillis(), null));

        long time = timeReasonPair.first() == -1 ? Duration.ofHours(1).toMillis() : timeReasonPair.first();

        if (time < 1) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Warn::getNullTime)
                    .build()
            );

            return;
        }

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Warn::getNullPlayer)
                    .build()
            );

            return;
        }

        long databaseTime = time + System.currentTimeMillis();
        String reason = timeReasonPair.second();

        Moderation warn = moderationService.warn(fTarget, databaseTime, reason, fPlayer.getId());
        if (warn == null) return;

        proxySender.send(fTarget, MessageType.SYSTEM_WARN);

        sendMessage(ModerationMetadata.<Localization.Command.Warn>builder()
                .sender(fTarget)
                .format(buildFormat(warn))
                .moderation(warn)
                .range(config().getRange())
                .destination(config().getDestination())
                .sound(getModuleSound())
                .proxy(dataOutputStream -> dataOutputStream.writeAsJson(warn))
                .integration(string -> moderationMessageFormatter.replacePlaceholders(string, FPlayer.UNKNOWN, warn))
                .build()
        );

        send(fPlayer, fTarget, warn);

        List<Moderation> warns = moderationService.getValidWarns(fTarget);
        if (warns.isEmpty()) return;

        int countWarns = warns.stream()
                .filter(moderation -> moderation.isValid() && !moderation.isExpired())
                .toList().size();

        String action = config().getActions().get(countWarns);
        if (StringUtils.isEmpty(action)) return;

        platformServerAdapter.dispatchCommand(Strings.CS.replace(action, "<target>", fTarget.getName()));
    }

    @Override
    public Command.Warn config() {
        return fileResolver.getCommand().getWarn();
    }

    @Override
    public Permission.Command.Warn permission() {
        return fileResolver.getPermission().getCommand().getWarn();
    }

    @Override
    public Localization.Command.Warn localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getWarn();
    }

    public BiFunction<FPlayer, Localization.Command.Warn, String> buildFormat(Moderation warn) {
        return (fReceiver, message) -> moderationMessageFormatter.replacePlaceholders(message.getServer(), fReceiver, warn);
    }

    public void send(FEntity fModerator, FPlayer fReceiver, Moderation warn) {
        if (isModuleDisabledFor(fModerator)) return;

        sendMessage(metadataBuilder()
                .sender(fReceiver)
                .format(s -> moderationMessageFormatter.replacePlaceholders(s.getPerson(), fReceiver, warn))
                .build()
        );
    }
}
