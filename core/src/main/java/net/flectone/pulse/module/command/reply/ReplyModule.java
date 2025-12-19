package net.flectone.pulse.module.command.reply;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ReplyModule extends AbstractModuleCommand<Localization.Command.Reply> {

    private final FileFacade fileFacade;
    private final TellModule tellModule;
    private final CommandParserProvider commandParserProvider;
    private final SoundPlayer soundPlayer;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptMessage = addPrompt(0, Localization.Command.Prompt::message);
        registerCommand(manager -> manager
                .permission(permission().name())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String receiverName = tellModule.getSenderReceiverMap().get(fPlayer.getUuid());
        if (receiverName == null) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Reply::nullReceiver)
                    .build()
            );

            return;
        }

        String message = getArgument(commandContext, 0);

        tellModule.send(fPlayer, receiverName, message);

        soundPlayer.play(getModuleSound(), fPlayer);
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_REPLY;
    }

    @Override
    public Command.Reply config() {
        return fileFacade.command().reply();
    }

    @Override
    public Permission.Command.Reply permission() {
        return fileFacade.permission().command().reply();
    }

    @Override
    public Localization.Command.Reply localization(FEntity sender) {
        return fileFacade.localization(sender).command().reply();
    }
}
