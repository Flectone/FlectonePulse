package net.flectone.pulse.module.command.symbol;

import lombok.Getter;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;

public abstract class SymbolModule extends AbstractModuleCommand<Localization.Command.Symbol> {

    @Getter
    private final Command.Symbol command;
    @Getter
    private final Permission.Command.Symbol permission;

    private final CommandUtil commandUtil;

    public SymbolModule(FileManager fileManager,
                        CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getSymbol(), null);

        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getSymbol();
        permission = fileManager.getPermission().getCommand().getSymbol();

        addPredicate(this::checkCooldown);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        String string = commandUtil.getString(1, arguments);

        builder(fPlayer)
                .format(s -> s.getFormat().replace("<message>", string))
                .message(string)
                .sound(getSound())
                .sendBuilt();
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
