package net.flectone.pulse.module.command.mark;

import lombok.Getter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.function.BiConsumer;

public abstract class MarkModule extends AbstractModuleCommand<Localization.Command> {

    @Getter private final Command.Mark command;
    @Getter private final Permission.Command.Mark permission;

    private final BiConsumer<FPlayer, NamedTextColor> markConsumer;
    private final CommandUtil commandUtil;

    public MarkModule(FileManager fileManager,
                      BiConsumer<FPlayer, NamedTextColor> markConsumer,
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

        NamedTextColor color = commandUtil.getByClassOrDefault(0, NamedTextColor.class, NamedTextColor.WHITE, arguments);

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
