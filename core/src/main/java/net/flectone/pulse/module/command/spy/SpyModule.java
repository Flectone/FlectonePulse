package net.flectone.pulse.module.command.spy;

import lombok.Getter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.database.dao.SettingDAO;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.PermissionUtil;

import java.util.function.Function;

public abstract class SpyModule extends AbstractModuleCommand<Localization.Command.Spy> {

    @Getter private final Command.Spy command;
    @Getter private final Permission.Command.Spy permission;

    private final SettingDAO settingDAO;
    private final CommandUtil commandUtil;
    private final PermissionUtil permissionUtil;

    public SpyModule(FileManager fileManager,
                     SettingDAO settingDAO,
                     CommandUtil commandUtil,
                     PermissionUtil permissionUtil) {
        super(localization -> localization.getCommand().getSpy(), null);

        this.settingDAO = settingDAO;
        this.commandUtil = commandUtil;
        this.permissionUtil = permissionUtil;

        command = fileManager.getCommand().getSpy();
        permission = fileManager.getPermission().getCommand().getSpy();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object commandArguments) {
        if (checkCooldown(fPlayer)) return;
        if (checkModulePredicates(fPlayer)) return;

        if (fPlayer.isSetting(FPlayer.Setting.SPY)) {
            fPlayer.removeSetting(FPlayer.Setting.SPY);
            settingDAO.delete(fPlayer, FPlayer.Setting.SPY);
        } else {
            fPlayer.setSetting(FPlayer.Setting.SPY);
            settingDAO.insertOrUpdate(fPlayer, FPlayer.Setting.SPY);
        }

        builder(fPlayer)
                .destination(command.getDestination())
                .format(s -> fPlayer.isSetting(FPlayer.Setting.SPY) ? s.getFormatTrue() : s.getFormatFalse())
                .sound(getSound())
                .sendBuilt();
    }

    public void spy(FPlayer fPlayer, String action, String string) {
        if (!isEnable()) return;

        builder(fPlayer)
                .range(command.getRange())
                .destination(command.getDestination())
                .filter(fReceiver -> !fPlayer.equals(fReceiver))
                .filter(fReceiver -> permissionUtil.has(fReceiver, getModulePermission()))
                .filter(fReceiver -> fReceiver.isSetting(FPlayer.Setting.SPY))
                .filter(FPlayer::isOnline)
                .tag(MessageTag.COMMAND_SPY)
                .format(replaceAction(action))
                .message((fResolver, s) -> string)
                .proxy(output -> {
                    output.writeUTF(action);
                    output.writeUTF(string);
                })
                .integration(s -> s
                        .replace("<action>", action)
                        .replace("<message>", string)
                )
                .sendBuilt();
    }

    public Function<Localization.Command.Spy, String> replaceAction(String action) {
        return message -> message.getFormatLog().replace("<action>", action);
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
