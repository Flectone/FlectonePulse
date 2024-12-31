package net.flectone.pulse.module.command.me;

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

public abstract class MeModule extends AbstractModuleCommand<Localization.Command.Me> {

    @Getter
    private final Command.Me command;
    @Getter
    private final Permission.Command.Me permission;

    private final CommandUtil commandUtil;

    public MeModule(FileManager fileManager,
                    CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getMe(), fPlayer -> fPlayer.is(FPlayer.Setting.ME));

        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getMe();
        permission = fileManager.getPermission().getCommand().getMe();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        String string = commandUtil.getString(0, arguments);

        builder(fPlayer)
                .tag(MessageTag.COMMAND_ME)
                .range(command.getRange())
                .format((fResolver, s) -> s.getFormat())
                .message((fResolver, s) -> string)
                .proxy(output -> output.writeUTF(string))
                .integration(s -> s.replace("<message>", string))
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
