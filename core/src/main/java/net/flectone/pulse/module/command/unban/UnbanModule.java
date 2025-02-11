package net.flectone.pulse.module.command.unban;

import com.google.gson.Gson;
import lombok.Getter;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.database.dao.ModerationDAO;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.MessageTag;

import java.util.ArrayList;
import java.util.List;

public abstract class UnbanModule extends AbstractModuleCommand<Localization.Command.Unban> {

    @Getter private final Command.Unban command;
    @Getter private final Permission.Command.Unban permission;

    private final FPlayerDAO fPlayerDAO;
    private final ModerationDAO moderationDAO;
    private final CommandUtil commandUtil;
    private final Gson gson;

    public UnbanModule(FileManager fileManager,
                       FPlayerDAO fPlayerDAO,
                       ModerationDAO moderationDAO,
                       CommandUtil commandUtil,
                       Gson gson) {
        super(localization -> localization.getCommand().getUnban(), null);

        this.fPlayerDAO = fPlayerDAO;
        this.moderationDAO = moderationDAO;
        this.commandUtil = commandUtil;
        this.gson = gson;

        command = fileManager.getCommand().getUnban();
        permission = fileManager.getPermission().getCommand().getUnban();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;

        String target = commandUtil.getString(0, arguments);
        int id = commandUtil.getByClassOrDefault(1, Integer.class, -1, arguments);

        unban(fPlayer, target, id);
    }

    public void unban(FPlayer fPlayer, String target, int id) {
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayerDAO.getFPlayer(target);
        if (fTarget.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Unban::getNullPlayer)
                    .sendBuilt();
            return;
        }

        List<Moderation> bans = new ArrayList<>();

        if (id == -1) {
            bans.addAll(moderationDAO.getValidModerations(fTarget, Moderation.Type.BAN));
        } else {
            moderationDAO.getValidModerations(fTarget, Moderation.Type.BAN).stream()
                    .filter(moderation -> moderation.getId() == id)
                    .findAny()
                    .ifPresent(bans::add);
        }

        if (bans.isEmpty()) {
            builder(fPlayer)
                    .format(Localization.Command.Unban::getNotBanned)
                    .sendBuilt();
            return;
        }

        for (Moderation ban : bans) {
            moderationDAO.updateInvalidModeration(ban);
        }

        builder(fTarget)
                .tag(MessageTag.COMMAND_UNBAN)
                .destination(command.getDestination())
                .range(command.getRange())
                .filter(filter -> filter.is(FPlayer.Setting.BAN))
                .format(unwarn -> unwarn.getFormat().replace("<moderator>", fPlayer.getName()))
                .proxy(output -> output.writeUTF(gson.toJson(fPlayer)))
                .integration(s -> s.replace("<moderator>", fPlayer.getName()))
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
