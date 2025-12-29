package net.flectone.pulse.module.command.kick;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.ModerationMetadata;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import org.incendo.cloud.context.CommandContext;

import java.util.Optional;
import java.util.function.BiFunction;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class KickModule extends AbstractModuleCommand<Localization.Command.Kick> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final CommandParserProvider commandParserProvider;
    private final MessagePipeline messagePipeline;
    private final PacketSender packetSender;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::player);
        String promptMessage = addPrompt(1, Localization.Command.Prompt::message);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptPlayer, commandParserProvider.playerParser())
                .optional(promptMessage, commandParserProvider.nativeMessageParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String playerName = getArgument(commandContext, 0);
        FPlayer fTarget = fPlayerService.getFPlayer(playerName);
        if (!fTarget.isOnline()) {
            sendErrorMessage( metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Kick::nullPlayer)
                    .build()
            );

            return;
        }

        String promptMessage = getPrompt(1);
        Optional<String> optionalReason = commandContext.optional(promptMessage);
        String reason = optionalReason.orElse(null);

        Moderation kick = moderationService.kick(fTarget, reason, fPlayer.getId());
        if (kick == null) return;

        kick(fPlayer, fTarget, kick);

        sendMessage(ModerationMetadata.<Localization.Command.Kick>builder()
                .sender(fTarget)
                .format(buildFormat(kick))
                .moderation(kick)
                .destination(config().destination())
                .range(config().range())
                .sound(soundOrThrow())
                .proxy(dataOutputStream -> dataOutputStream.writeAsJson(kick))
                .integration(string -> moderationMessageFormatter.replacePlaceholders(string, FPlayer.UNKNOWN, kick))
                .build()
        );

    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_KICK;
    }

    @Override
    public Command.Kick config() {
        return fileFacade.command().kick();
    }

    @Override
    public Permission.Command.Kick permission() {
        return fileFacade.permission().command().kick();
    }

    @Override
    public Localization.Command.Kick localization(FEntity sender) {
        return fileFacade.localization(sender).command().kick();
    }

    public BiFunction<FPlayer, Localization.Command.Kick, String> buildFormat(Moderation kick) {
        return (fReceiver, message) -> moderationMessageFormatter.replacePlaceholders(message.server(), fReceiver, kick);
    }

    public void kick(FEntity fModerator, FPlayer fReceiver, Moderation kick) {
        if (isModuleDisabledFor(fModerator)) return;

        String format = moderationMessageFormatter.replacePlaceholders(localization(fReceiver).person(), fReceiver, kick);
        MessageContext messageContext = messagePipeline.createContext(fReceiver, format);

        packetSender.send(fReceiver, new WrapperPlayServerDisconnect(messagePipeline.build(messageContext)));
    }
}
