package net.flectone.pulse.module.command.reply;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.platform.controller.CommandModuleController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.incendo.cloud.context.CommandContext;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ReplyModule implements AbstractModuleCommand<Localization.Command.Reply> {

    private final FileFacade fileFacade;
    private final TellModule tellModule;
    private final CommandParserProvider commandParserProvider;
    private final SoundPlayer soundPlayer;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final CommandModuleController commandModuleController;

    @Override
    public void onEnable() {
        String promptMessage = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::message);
        commandModuleController.registerCommand(this, manager -> manager
                .permission(permission().name())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
        );
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String receiverName = tellModule.getReceiverFor(fPlayer);
        if (receiverName == null) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Reply>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Reply::nullReceiver)
                    .build()
            );

            return;
        }

        String message = commandModuleController.getArgument(this, commandContext, 0);

        tellModule.send(fPlayer, receiverName, message);

        soundPlayer.play(soundOrThrow(), fPlayer);
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_REPLY;
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
