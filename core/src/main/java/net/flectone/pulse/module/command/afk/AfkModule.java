package net.flectone.pulse.module.command.afk;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

@Singleton
public class AfkModule extends AbstractModuleCommand<Localization.Command> {

    private final Command.Afk command;
    private final Permission.Command.Afk permission;

    private final net.flectone.pulse.module.message.afk.AfkModule afkModule;
    private final CommandRegistry commandRegistry;

    @Inject
    public AfkModule(FileResolver fileResolver,
                     net.flectone.pulse.module.message.afk.AfkModule afkModule,
                     CommandRegistry commandRegistry) {
        super(Localization::getCommand, fPlayer -> fPlayer.isSetting(FPlayer.Setting.AFK));

        this.afkModule = afkModule;
        this.commandRegistry = commandRegistry;

        command = fileResolver.getCommand().getAfk();
        permission = fileResolver.getPermission().getCommand().getAfk();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> !afkModule.isEnable());
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
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        if (fPlayer.isSetting(FPlayer.Setting.AFK_SUFFIX)) {
            afkModule.remove("afk", fPlayer);
        } else {
            afkModule.setAfk(fPlayer);
        }

        playSound(fPlayer);
    }
}