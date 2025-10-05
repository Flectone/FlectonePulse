package net.flectone.pulse.module.command.mute;

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
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.type.tuple.Pair;

import java.time.Duration;
import java.util.Optional;
import java.util.function.BiFunction;

@Singleton
public class MuteModule extends AbstractModuleCommand<Localization.Command.Mute> {

    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final CommandParserProvider commandParserProvider;
    private final ProxySender proxySender;

    @Inject
    public MuteModule(FileResolver fileResolver,
                      FPlayerService fPlayerService,
                      ModerationService moderationService,
                      ModerationMessageFormatter moderationMessageFormatter,
                      CommandParserProvider commandParserProvider,
                      ProxySender proxySender) {
        super(MessageType.COMMAND_MUTE);

        this.fileResolver = fileResolver;
        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.moderationMessageFormatter = moderationMessageFormatter;
        this.commandParserProvider = commandParserProvider;
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
                    .format(Localization.Command.Mute::getNullTime)
                    .build()
            );

            return;
        }

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Mute::getNullPlayer)
                    .build()
            );

            return;
        }

        long databaseTime = time + System.currentTimeMillis();
        String reason = timeReasonPair.second();

        Moderation mute = moderationService.mute(fTarget, databaseTime, reason, fPlayer.getId());
        if (mute == null) return;

        proxySender.send(fTarget, MessageType.SYSTEM_MUTE);

        sendMessage(ModerationMetadata.<Localization.Command.Mute>builder()
                .sender(fTarget)
                .format(buildFormat(mute))
                .moderation(mute)
                .range(config().getRange())
                .destination(config().getDestination())
                .sound(getModuleSound())
                .proxy(dataOutputStream -> dataOutputStream.writeAsJson(mute))
                .integration(string -> moderationMessageFormatter.replacePlaceholders(string, FPlayer.UNKNOWN, mute))
                .build()
        );

        sendForTarget(fPlayer, fTarget, mute);
    }

    @Override
    public Command.Mute config() {
        return fileResolver.getCommand().getMute();
    }

    @Override
    public Permission.Command.Mute permission() {
        return fileResolver.getPermission().getCommand().getMute();
    }

    @Override
    public Localization.Command.Mute localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getMute();
    }

    public BiFunction<FPlayer, Localization.Command.Mute, String> buildFormat(Moderation mute) {
        return (fReceiver, message) -> moderationMessageFormatter.replacePlaceholders(message.getServer(), fReceiver, mute);
    }

    public void sendForTarget(FEntity fModerator, FPlayer fReceiver, Moderation mute) {
        if (isModuleDisabledFor(fModerator)) return;

        sendMessage(metadataBuilder()
                .sender(fReceiver)
                .format(s -> moderationMessageFormatter.replacePlaceholders(s.getPerson(), fReceiver, mute))
                .build()
        );
    }
}
