package net.flectone.pulse.module.command.reply;

import lombok.Getter;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;

public abstract class ReplyModule extends AbstractModuleCommand<Localization.Command.Reply> {

    @Getter
    private final Command.Reply command;
    @Getter
    private final Permission.Command.Reply permission;

    private final TellModule tellModule;
    private final CommandUtil commandUtil;

    public ReplyModule(FileManager fileManager,
                       TellModule tellModule,
                       CommandUtil componentUtil) {
        super(localization -> localization.getCommand().getReply(), fPlayer -> fPlayer.is(FPlayer.Setting.REPLY));

        this.tellModule = tellModule;
        this.commandUtil = componentUtil;

        command = fileManager.getCommand().getReply();
        permission = fileManager.getPermission().getCommand().getReply();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        String receiverName = tellModule.getSenderReceiverMap().get(fPlayer.getUuid());
        if (receiverName == null) {
            builder(fPlayer)
                    .format(Localization.Command.Reply::getNullReceiver)
                    .sendBuilt();
            return;
        }

        String message = commandUtil.getString(0, arguments);

        tellModule.send(fPlayer, receiverName, message);
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
