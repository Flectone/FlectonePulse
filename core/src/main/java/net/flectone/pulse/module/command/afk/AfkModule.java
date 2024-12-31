package net.flectone.pulse.module.command.afk;

import lombok.Getter;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;

@Getter
public abstract class AfkModule extends AbstractModuleCommand<Localization.Command> {

    private final Command.Afk command;
    private final Permission.Command.Afk permission;

    private final net.flectone.pulse.module.message.contact.afk.AfkModule afkModule;

    public AfkModule(FileManager fileManager,
                     net.flectone.pulse.module.message.contact.afk.AfkModule afkModule) {
        super(Localization::getCommand, fPlayer -> fPlayer.is(FPlayer.Setting.AFK));

        this.afkModule = afkModule;

        command = fileManager.getCommand().getAfk();
        permission = fileManager.getPermission().getCommand().getAfk();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> !afkModule.isEnable());
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        if (fPlayer.getAfkSuffix() == null) {
            afkModule.setAfk(fPlayer);
        } else {
            afkModule.remove("afk", fPlayer);
        }

        playSound(fPlayer);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}