package net.flectone.pulse.module.command.deletemessage;

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
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.deletemessage.model.DeletemessageMetadata;
import net.flectone.pulse.module.message.format.moderation.delete.DeleteModule;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.UUIDParser;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DeletemessageModule implements ModuleCommand<Localization.Command.Deletemessage> {

    private final FileFacade fileFacade;
    private final DeleteModule deleteModule;
    private final ProxySender proxySender;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;

    @Override
    public void onEnable() {
        String promptId = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::id);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptId, UUIDParser.uuidParser())
        );
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        UUID uuid = commandModuleController.getArgument(this, commandContext, 0);
        if (!deleteModule.remove(fPlayer, uuid)) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Deletemessage>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Deletemessage::nullMessage)
                    .build()
            );

            return;
        }

        proxySender.send(fPlayer, ModuleName.COMMAND_DELETEMESSAGE,
                dataOutputStream -> dataOutputStream.writeUTF(uuid.toString()),
                UUID.randomUUID()
        );

        messageDispatcher.dispatch(this, DeletemessageMetadata.<Localization.Command.Deletemessage>builder()
                .base(EventMetadata.<Localization.Command.Deletemessage>builder()
                        .sender(fPlayer)
                        .format(Localization.Command.Deletemessage::format)
                        .destination(config().destination())
                        .sound(soundOrThrow())
                        .build()
                )
                .deletedUUID(uuid)
                .build()
        );
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_DELETEMESSAGE;
    }

    @Override
    public Command.Deletemessage config() {
        return fileFacade.command().deletemessage();
    }

    @Override
    public Permission.Command.Deletemessage permission() {
        return fileFacade.permission().command().deletemessage();
    }

    @Override
    public Localization.Command.Deletemessage localization(FEntity sender) {
        return fileFacade.localization(sender).command().deletemessage();
    }
}
