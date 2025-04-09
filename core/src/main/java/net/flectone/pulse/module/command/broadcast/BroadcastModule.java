package net.flectone.pulse.module.command.broadcast;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

@Singleton
public class BroadcastModule extends AbstractModuleCommand<Localization.Command.Broadcast> {

    @Getter private final Command.Broadcast command;
    private final Permission.Command.Broadcast permission;

    private final CommandRegistry commandRegistry;

    @Inject
    public BroadcastModule(FileManager fileManager,
                           CommandRegistry commandRegistry) {
        super(localization -> localization.getCommand().getBroadcast(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.BROADCAST));

        this.commandRegistry = commandRegistry;

        command = fileManager.getCommand().getBroadcast();
        permission = fileManager.getPermission().getCommand().getBroadcast();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    protected boolean isConfigEnable() {
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

        String promptMessage = getPrompt().getMessage();
        String message = commandContext.get(promptMessage);

        builder(fPlayer)
                .range(command.getRange())
                .destination(command.getDestination())
                .tag(MessageTag.COMMAND_BROADCAST)
                .format(Localization.Command.Broadcast::getFormat)
                .message((fResolver, s) -> message)
                .proxy(output -> output.writeUTF(message))
                .integration(s -> s.replace("<message>", message))
                .sound(getSound())
                .sendBuilt();
    }
}
