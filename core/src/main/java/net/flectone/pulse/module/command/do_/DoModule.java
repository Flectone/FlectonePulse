package net.flectone.pulse.module.command.do_;

import lombok.Getter;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;

public abstract class DoModule extends AbstractModuleCommand<Localization.Command.Do> {

    @Getter
    private final Command.Do command;
    @Getter
    private final Permission.Command.Do permission;

    private final CommandUtil commandUtil;

    public DoModule(FileManager fileManager,
                    CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getDo(), fPlayer -> fPlayer.is(FPlayer.Setting.DO));

        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getDo();
        permission = fileManager.getPermission().getCommand().getDo();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        String message = commandUtil.getString(0, arguments);

        builder(fPlayer)
                .range(command.getRange())
                .tag(MessageTag.COMMAND_DO)
                .format(Localization.Command.Do::getFormat)
                .message(message)
                .proxy(output -> output.writeUTF(message))
                .integration(s -> s.replace("<message>", message))
                .sound(getSound())
                .sendBuilt();
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