package net.flectone.pulse.module.command.warn;

import com.google.gson.Gson;
import lombok.Getter;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.database.dao.ModerationDAO;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.ModerationUtil;

import java.util.List;
import java.util.function.BiFunction;

public abstract class WarnModule extends AbstractModuleCommand<Localization.Command.Warn> {

    @Getter private final Command.Warn command;
    @Getter private final Permission.Command.Warn permission;

    private final FPlayerDAO fPlayerDAO;
    private final ModerationDAO moderationDAO;
    private final CommandUtil commandUtil;
    private final ModerationUtil moderationUtil;
    private final Gson gson;

    public WarnModule(FileManager fileManager,
                      FPlayerDAO fPlayerDAO,
                      ModerationDAO moderationDAO,
                      CommandUtil commandUtil,
                      ModerationUtil moderationUtil,
                      Gson gson) {
        super(localization -> localization.getCommand().getWarn(), fPlayer -> fPlayer.is(FPlayer.Setting.WARN));

        this.fPlayerDAO = fPlayerDAO;
        this.moderationDAO = moderationDAO;
        this.commandUtil = commandUtil;
        this.moderationUtil = moderationUtil;
        this.gson = gson;

        command = fileManager.getCommand().getWarn();
        permission = fileManager.getPermission().getCommand().getWarn();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkMute(fPlayer)) return;

        String target = commandUtil.getString(0, arguments);
        long time = commandUtil.getInteger(1, arguments) * 1000L;

        if (time < 1) {
            builder(fPlayer)
                    .format(Localization.Command.Warn::getNullTime)
                    .sendBuilt();
            return;
        }

        String reason = commandUtil.getString(2, arguments);

        FPlayer fTarget = fPlayerDAO.getFPlayer(target);
        if (fTarget.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Warn::getNullPlayer)
                    .sendBuilt();
            return;
        }

        long databaseTime = time != -1 ? time + System.currentTimeMillis() : -1;

        Moderation warn = moderationDAO.insertModeration(fTarget, databaseTime, reason, fPlayer.getId(), Moderation.Type.WARN);
        if (warn == null) return;

        builder(fTarget)
                .range(command.getRange())
                .destination(command.getDestination())
                .tag(MessageTag.COMMAND_WARN)
                .format(buildFormat(warn))
                .proxy(output -> {
                    output.writeUTF(gson.toJson(fPlayer));
                    output.writeUTF(gson.toJson(warn));
                })
                .integration(s -> moderationUtil.replacePlaceholders(s, FPlayer.UNKNOWN, warn))
                .sound(getSound())
                .sendBuilt();

        send(fPlayer, fTarget, warn);

        List<Moderation> warns = moderationDAO.getModerations(fTarget, Moderation.Type.WARN);
        if (warns.isEmpty()) return;

        int countWarns = warns.stream()
                .filter(moderation -> moderation.isValid() && !moderation.isExpired())
                .toList().size();

        String action = command.getActions().get(countWarns);
        if (action == null) return;

        commandUtil.dispatch(action.replace("<target>", fTarget.getName()));
    }

    public BiFunction<FPlayer, Localization.Command.Warn, String> buildFormat(Moderation warn) {
        return (fReceiver, message) ->  moderationUtil.replacePlaceholders(message.getServer(), fReceiver, warn);
    }

    public void send(FEntity fModerator, FPlayer fReceiver, Moderation warn) {
        if (checkModulePredicates(fModerator)) return;

        builder(fReceiver)
                .format(s -> moderationUtil.replacePlaceholders(s.getPerson(), fReceiver, warn))
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
