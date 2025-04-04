package net.flectone.pulse.module.command.reply;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.util.DisableAction;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

@Singleton
public class ReplyModule extends AbstractModuleCommand<Localization.Command.Reply> {

    private final Command.Reply command;
    private final Permission.Command.Reply permission;

    private final TellModule tellModule;
    private final CommandRegistry commandRegistry;

    @Inject
    public ReplyModule(FileManager fileManager,
                       TellModule tellModule,
                       CommandRegistry commandRegistry) {
        super(localization -> localization.getCommand().getReply(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.REPLY));

        this.tellModule = tellModule;
        this.commandRegistry = commandRegistry;

        command = fileManager.getCommand().getReply();
        permission = fileManager.getPermission().getCommand().getReply();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptMessage = getPrompt().getMessage();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .required(promptMessage, commandRegistry.nativeMessageParser())
                        .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        String receiverName = tellModule.getSenderReceiverMap().get(fPlayer.getUuid());
        if (receiverName == null) {
            builder(fPlayer)
                    .format(Localization.Command.Reply::getNullReceiver)
                    .sendBuilt();
            return;
        }

        String promptMessage = getPrompt().getMessage();
        String message = commandContext.get(promptMessage);

        tellModule.send(fPlayer, receiverName, message);
    }
}
