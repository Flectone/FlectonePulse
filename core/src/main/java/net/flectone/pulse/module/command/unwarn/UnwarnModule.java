package net.flectone.pulse.module.command.unwarn;

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

public abstract class UnwarnModule extends AbstractModuleCommand<Localization.Command.Unwarn> {

    @Getter private final Command.Unwarn command;
    @Getter private final Permission.Command.Unwarn permission;

    private final FPlayerDAO fPlayerDAO;
    private final ModerationDAO moderationDAO;
    private final CommandUtil commandUtil;
    private final Gson gson;

    public UnwarnModule(FileManager fileManager,
                        FPlayerDAO fPlayerDAO,
                        ModerationDAO moderationDAO,
                        CommandUtil commandUtil,
                        Gson gson) {
        super(localization -> localization.getCommand().getUnwarn(), null);

        this.fPlayerDAO = fPlayerDAO;
        this.moderationDAO = moderationDAO;
        this.commandUtil = commandUtil;
        this.gson = gson;

        command = fileManager.getCommand().getUnwarn();
        permission = fileManager.getPermission().getCommand().getUnwarn();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;

        String target = commandUtil.getString(0, arguments);
        int id = commandUtil.getByClassOrDefault(1, Integer.class, -1, arguments);

        unwarn(fPlayer, target, id);
    }

    public void unwarn(FPlayer fPlayer, String target, int id) {
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayerDAO.getFPlayer(target);
        if (fTarget.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Unwarn::getNullPlayer)
                    .sendBuilt();
            return;
        }

        List<Moderation> warns = new ArrayList<>();

        if (id == -1) {
            warns.addAll(moderationDAO.getValidModerations(fTarget, Moderation.Type.WARN));
        } else {
            moderationDAO.getValidModerations(fTarget, Moderation.Type.WARN).stream()
                    .filter(warn -> warn.getId() == id)
                    .findAny()
                    .ifPresent(warns::add);
        }

        if (warns.isEmpty()) {
            builder(fPlayer)
                    .format(Localization.Command.Unwarn::getNotWarned)
                    .sendBuilt();
            return;
        }

        for (Moderation warn : warns) {
            moderationDAO.updateInvalidModeration(warn);
        }

        builder(fTarget)
                .tag(MessageTag.COMMAND_UNWARN)
                .destination(command.getDestination())
                .range(command.getRange())
                .filter(filter -> filter.is(FPlayer.Setting.WARN))
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
