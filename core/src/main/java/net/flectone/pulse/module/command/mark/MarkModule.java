package net.flectone.pulse.module.command.mark;

import lombok.Getter;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;

import java.util.function.BiConsumer;

public abstract class MarkModule extends AbstractModuleCommand<Localization.Command> {

    @Getter
    private final Command.Mark command;
    @Getter
    private final Permission.Command.Mark permission;

    private final BiConsumer<FPlayer, String> markConsumer;
    private final CommandUtil commandUtil;

    public MarkModule(FileManager fileManager,
                      BiConsumer<FPlayer, String> markConsumer,
                      CommandUtil commandUtil) {
        super(Localization::getCommand, null);

        this.markConsumer = markConsumer;
        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getMark();
        permission = fileManager.getPermission().getCommand().getMark();

        addPredicate(this::checkCooldown);
        addPredicate(this::checkMute);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        String color = commandUtil.getByClassOrDefault(0, String.class, "white", arguments);

        markConsumer.accept(fPlayer, color);
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
