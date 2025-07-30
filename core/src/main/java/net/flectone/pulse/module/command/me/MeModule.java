package net.flectone.pulse.module.command.me;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.constant.DisableSource;
import net.flectone.pulse.constant.MessageType;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

@Singleton
public class MeModule extends AbstractModuleCommand<Localization.Command.Me> {

    @Getter private final Command.Me command;
    private final Permission.Command.Me permission;
    private final CommandRegistry commandRegistry;

    @Inject
    public MeModule(FileResolver fileResolver,
                    CommandRegistry commandRegistry) {
        super(localization -> localization.getCommand().getMe(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.ME));

        this.command = fileResolver.getCommand().getMe();
        this.permission = fileResolver.getPermission().getCommand().getMe();
        this.commandRegistry = commandRegistry;
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void onEnable() {
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

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableSource.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        String promptMessage = getPrompt().getMessage();
        String string = commandContext.get(promptMessage);

        builder(fPlayer)
                .tag(MessageType.COMMAND_ME)
                .destination(command.getDestination())
                .range(command.getRange())
                .format(Localization.Command.Me::getFormat)
                .message(string)
                .proxy(output -> output.writeUTF(string))
                .integration(s -> s.replace("<message>", string))
                .sound(getSound())
                .sendBuilt();
    }
}
