package net.flectone.pulse.module.command.afk;

import lombok.Getter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;

@Getter
public abstract class AfkModule extends AbstractModuleCommand<Localization.Command> {

    private final Command.Afk command;
    private final Permission.Command.Afk permission;

    private final net.flectone.pulse.module.message.afk.AfkModule afkModule;
    private final CommandUtil commandUtil;

    public AfkModule(FileManager fileManager,
                     net.flectone.pulse.module.message.afk.AfkModule afkModule,
                     CommandUtil commandUtil) {
        super(Localization::getCommand, fPlayer -> fPlayer.isSetting(FPlayer.Setting.AFK));

        this.afkModule = afkModule;
        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getAfk();
        permission = fileManager.getPermission().getCommand().getAfk();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> !afkModule.isEnable());
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        if (fPlayer.isSetting(FPlayer.Setting.AFK_SUFFIX)) {
            afkModule.remove("afk", fPlayer);
        } else {
            afkModule.setAfk(fPlayer);
        }

        playSound(fPlayer);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        getCommand().getAliases().forEach(commandUtil::unregister);

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}