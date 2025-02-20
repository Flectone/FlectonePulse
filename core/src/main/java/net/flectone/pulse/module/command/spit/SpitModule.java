package net.flectone.pulse.module.command.spit;

import lombok.Getter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;

import java.util.function.Consumer;

public abstract class SpitModule extends AbstractModuleCommand<Localization.Command> {

    @Getter private final Command.Spit command;
    @Getter private final Permission.Command.Spit permission;

    private final Consumer<FPlayer> spitConsumer;
    private final CommandUtil commandUtil;

    public SpitModule(FileManager fileManager,
                      Consumer<FPlayer> spitConsumer,
                      CommandUtil commandUtil) {
        super(Localization::getCommand, fPlayer -> fPlayer.is(FPlayer.Setting.SPIT));

        this.spitConsumer = spitConsumer;
        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getSpit();
        permission = fileManager.getPermission().getCommand().getSpit();

        addPredicate(this::checkCooldown);
        addPredicate(this::checkMute);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        spitConsumer.accept(fPlayer);

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