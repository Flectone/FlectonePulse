package net.flectone.pulse.module.command.mute;

import com.google.gson.Gson;
import lombok.Getter;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.TimeUtil;

import java.sql.SQLException;
import java.util.function.BiFunction;

public abstract class MuteModule extends AbstractModuleCommand<Localization.Command.Mute> {

    @Getter
    private final Command.Mute command;
    @Getter
    private final Permission.Command.Mute permission;

    private final ThreadManager threadManager;
    private final FPlayerManager fPlayerManager;
    private final CommandUtil commandUtil;
    private final TimeUtil timeUtil;
    private final Gson gson;

    public MuteModule(FileManager fileManager,
                      ThreadManager threadManager,
                      FPlayerManager fPlayerManager,
                      CommandUtil commandUtil,
                      TimeUtil timeUtil,
                      Gson gson) {
        super(localization -> localization.getCommand().getMute(), fPlayer -> fPlayer.is(FPlayer.Setting.MUTE));

        this.threadManager = threadManager;
        this.fPlayerManager = fPlayerManager;
        this.commandUtil = commandUtil;
        this.timeUtil = timeUtil;
        this.gson = gson;

        command = fileManager.getCommand().getMute();
        permission = fileManager.getPermission().getCommand().getMute();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;
        if (checkMute(fPlayer)) return;

        String target = commandUtil.getString(0, arguments);
        long time = commandUtil.getInteger(1, arguments) * 1000L;

        if (time < 1) {
            builder(fPlayer)
                    .format(Localization.Command.Mute::getNullTime)
                    .sendBuilt();
            return;
        }

        String reason =  commandUtil.getString(2, arguments);
        threadManager.runDatabase(database -> {

            FPlayer fTarget = database.getFPlayer(target);
            if (fTarget.isUnknown()) {
                builder(fPlayer)
                        .format(Localization.Command.Mute::getNullPlayer)
                        .sendBuilt();
                return;
            }

            long databaseTime = time != -1 ? time + System.currentTimeMillis() : -1;

            Moderation mute = database.insertModeration(fTarget, databaseTime, reason, fPlayer.getId(), Moderation.Type.MUTE);
            if (mute == null) return;

            if (!fPlayerManager.get(fTarget.getUuid()).isUnknown()) {
                fPlayerManager.get(fTarget.getUuid()).getMutes().add(mute);
            }

            sendForTarget(fPlayer, fTarget, mute);

            builder(fPlayer)
                    .range(command.getRange())
                    .tag(MessageTag.COMMAND_MUTE)
                    .format(replaceTarget(fTarget.getName(), time))
                    .message((fReceiver, s) -> s.getReasons().getConstant(reason))
                    .proxy(output -> {
                        output.writeUTF(gson.toJson(fTarget));
                        output.writeUTF(gson.toJson(mute));
                    })
                    .integration(s -> s
                            .replace("<reason>", resolveLocalization().getReasons().getConstant(reason))
                            .replace("<target>", fTarget.getName())
                            .replace("<time>", timeUtil.format(null, time))
                    )
                    .sound(getSound())
                    .sendBuilt();
        });
    }

    public BiFunction<FPlayer, Localization.Command.Mute, String> replaceTarget(String target, long time) {
        return (fReceiver, message) -> timeUtil.format(fReceiver, time, message.getGlobal().replace("<target>", target));
    }

    public abstract void sendForTarget(FEntity fPlayer, FPlayer fTarget, Moderation mute) throws SQLException;

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
