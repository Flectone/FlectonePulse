package net.flectone.pulse.module.command.deletemessage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.deletemessage.model.DeletemessageMetadata;
import net.flectone.pulse.module.message.format.moderation.delete.DeleteModule;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.UUIDParser;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DeletemessageModule extends AbstractModuleCommand<Localization.Command.Deletemessage> {

    private final FileResolver fileResolver;
    private final DeleteModule deleteModule;
    private final ProxySender proxySender;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptId = addPrompt(0, Localization.Command.Prompt::getId);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().getName())
                .required(promptId, UUIDParser.uuidParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        UUID uuid = getArgument(commandContext, 0);
        if (!deleteModule.remove(fPlayer, uuid)) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Deletemessage::getNullMessage)
                    .build()
            );

            return;
        }

        proxySender.send(fPlayer, MessageType.COMMAND_DELETE,
                dataOutputStream -> dataOutputStream.writeUTF(uuid.toString()),
                UUID.randomUUID()
        );

        sendMessage(DeletemessageMetadata.<Localization.Command.Deletemessage>builder()
                .sender(fPlayer)
                .format(Localization.Command.Deletemessage::getFormat)
                .deletedUUID(uuid)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_DELETE;
    }

    @Override
    public Command.Deletemessage config() {
        return fileResolver.getCommand().getDeletemessage();
    }

    @Override
    public Permission.Command.Deletemessage permission() {
        return fileResolver.getPermission().getCommand().getDeletemessage();
    }

    @Override
    public Localization.Command.Deletemessage localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getDeletemessage();
    }
}
