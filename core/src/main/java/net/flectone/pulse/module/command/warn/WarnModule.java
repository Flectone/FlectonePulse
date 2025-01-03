package net.flectone.pulse.module.command.warn;

import com.google.gson.Gson;
import lombok.Getter;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.TimeUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.function.BiFunction;

public abstract class WarnModule extends AbstractModuleCommand<Localization.Command.Warn> {

    @Getter
    private final Command.Warn command;
    @Getter
    private final Permission.Command.Warn permission;

    private final ThreadManager threadManager;
    private final TimeUtil timeUtil;
    private final CommandUtil commandUtil;
    private final Gson gson;

    public WarnModule(FileManager fileManager,
                      ThreadManager threadManager,
                      TimeUtil timeUtil,
                      CommandUtil commandUtil,
                      Gson gson) {
        super(localization -> localization.getCommand().getWarn(), fPlayer -> fPlayer.is(FPlayer.Setting.WARN));

        this.threadManager = threadManager;
        this.timeUtil = timeUtil;
        this.commandUtil = commandUtil;
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

        threadManager.runDatabase(database -> {
            FPlayer fTarget = database.getFPlayer(target);
            if (fTarget.isUnknown()) {
                builder(fPlayer)
                        .format(Localization.Command.Warn::getNullPlayer)
                        .sendBuilt();
                return;
            }

            long databaseTime = time != -1 ? time + System.currentTimeMillis() : -1;

            Moderation warn = database.insertModeration(fTarget, databaseTime, reason, fPlayer.getId(), Moderation.Type.WARN);
            if (warn == null) return;

            builder(fPlayer)
                    .range(command.getRange())
                    .tag(MessageTag.COMMAND_WARN)
                    .format(replaceTarget(fTarget.getName(), time))
                    .message((fResolver, s) -> s.getReasons().getConstant(reason))
                    .proxy(output -> {
                        output.writeUTF(gson.toJson(fTarget));
                        output.writeUTF(gson.toJson(warn));
                    })
                    .integration(s -> s
                            .replace("<reason>", resolveLocalization().getReasons().getConstant(reason))
                            .replace("<target>", fTarget.getName())
                            .replace("<time>", timeUtil.format(null, time))
                    )
                    .sound(getSound())
                    .sendBuilt();

            send(fPlayer, fTarget, warn);

            List<Moderation> warns = database.getModerations(fTarget, Moderation.Type.WARN);
            if (warns.isEmpty()) return;

            int countWarns = warns.stream()
                    .filter(moderation -> moderation.isValid() && !moderation.isExpired())
                    .toList().size();

            String action = command.getActions().get(countWarns);
            if (action == null) return;

            commandUtil.dispatch(action.replace("<target>", fTarget.getName()));
        });
    }

    public BiFunction<FPlayer, Localization.Command.Warn, String> replaceTarget(String target, long time) {
        return (fReceiver, message) -> timeUtil.format(fReceiver, time, message.getGlobal().replace("<target>", target));
    }

    public void send(FEntity fPlayer, FPlayer fTarget, Moderation warn) throws SQLException {
        if (checkModulePredicates(fPlayer)) return;

        builder(fPlayer)
                .receiver(fTarget)
                .format(s -> timeUtil.format(fTarget, warn.getRemainingTime(), s.getPlayer()
                        .replace("<message>", s.getReasons().getConstant(warn.getReason()))
                        .replace("<moderator>", fPlayer.getName()))
                )
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
