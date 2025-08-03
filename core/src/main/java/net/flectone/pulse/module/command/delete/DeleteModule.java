package net.flectone.pulse.module.command.delete;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.DisableSource;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.UUIDParser;

import java.util.UUID;

@Singleton
public class DeleteModule extends AbstractModuleCommand<Localization.Command.Delete> {

    private final Command.Delete command;
    private final Permission.Command.Delete permission;
    private final net.flectone.pulse.module.message.format.moderation.delete.DeleteModule deleteModule;
    private final ProxySender proxySender;

    @Inject
    public DeleteModule(FileResolver fileResolver,
                        net.flectone.pulse.module.message.format.moderation.delete.DeleteModule deleteModule,
                        ProxySender proxySender) {
        super(localization -> localization.getCommand().getDelete(), Command::getDelete);

        this.command = fileResolver.getCommand().getDelete();
        this.permission = fileResolver.getPermission().getCommand().getDelete();
        this.deleteModule = deleteModule;
        this.proxySender = proxySender;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptId = addPrompt(0, Localization.Command.Prompt::getId);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission.getName())
                .required(promptId, UUIDParser.uuidParser())
        );

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableSource.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        UUID uuid = getArgument(commandContext, 0);
        if (!deleteModule.remove(fPlayer, uuid)) {
            builder(fPlayer)
                    .format(Localization.Command.Delete::getNullMessage)
                    .sendBuilt();
            return;
        }

        proxySender.send(fPlayer, MessageType.COMMAND_DELETE, dataOutputStream ->
                dataOutputStream.writeUTF(uuid.toString())
        );

        builder(fPlayer)
                .destination(command.getDestination())
                .tag(MessageType.COMMAND_BALL)
                .format(Localization.Command.Delete::getFormat)
                .proxy(output -> output.writeUTF(uuid.toString()))
                .sound(getSound())
                .sendBuilt();
    }
}
