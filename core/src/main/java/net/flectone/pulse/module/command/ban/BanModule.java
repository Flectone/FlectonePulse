package net.flectone.pulse.module.command.ban;

import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerDisconnect;
import com.google.gson.Gson;
import lombok.Getter;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ListenerManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.ban.listener.BanPacketListener;
import net.flectone.pulse.util.*;
import net.kyori.adventure.text.Component;

import java.sql.SQLException;
import java.util.UUID;
import java.util.function.BiFunction;

public abstract class BanModule extends AbstractModuleCommand<Localization.Command.Ban> {

    @Getter
    private final Command.Ban command;
    @Getter
    private final Permission.Command.Ban permission;

    private final Database database;
    private final ThreadManager threadManager;
    private final FPlayerManager fPlayerManager;
    private final PermissionUtil permissionUtil;
    private final ListenerManager listenerManager;
    private final CommandUtil commandUtil;
    private final ComponentUtil componentUtil;
    private final PacketEventsUtil packetEventsUtil;
    private final TimeUtil timeUtil;
    private final FLogger fLogger;
    private final Gson gson;

    public BanModule(Database database,
                     FileManager fileManager,
                     FPlayerManager fPlayerManager,
                     PermissionUtil permissionUtil,
                     ThreadManager threadManager,
                     ListenerManager listenerManager,
                     CommandUtil commandUtil,
                     ComponentUtil componentUtil,
                     PacketEventsUtil packetEventsUtil,
                     TimeUtil timeUtil,
                     FLogger fLogger,
                     Gson gson) {
        super(localization -> localization.getCommand().getBan(), fPlayer -> fPlayer.is(FPlayer.Setting.BAN));

        this.database = database;
        this.threadManager = threadManager;
        this.fPlayerManager = fPlayerManager;
        this.permissionUtil = permissionUtil;
        this.listenerManager = listenerManager;
        this.commandUtil = commandUtil;
        this.componentUtil = componentUtil;
        this.packetEventsUtil = packetEventsUtil;
        this.timeUtil = timeUtil;
        this.fLogger = fLogger;
        this.gson = gson;

        command = fileManager.getCommand().getBan();
        permission = fileManager.getPermission().getCommand().getBan();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkCooldown(fPlayer)) return;
        if (checkMute(fPlayer)) return;
        if (checkModulePredicates(fPlayer)) return;

        String target = commandUtil.getString(0, arguments);

        long time;
        String reason;
        try {
            time = commandUtil.getInteger(1, arguments);
            if (time != -1) {
                time *= 1000L;
                reason = commandUtil.getString(2, arguments);
            } else {
                reason = commandUtil.getString(1, arguments);
            }
        } catch (ClassCastException | NullPointerException e) {
            time = -1;
            reason = commandUtil.getString(1, arguments);
        }

        if (reason != null && commandUtil.getString(2, arguments) != null && !reason.equalsIgnoreCase(commandUtil.getString(2, arguments))) {
            reason += " " + commandUtil.getString(2, arguments);
        }

        if (time != -1 && time < 1) {
            builder(fPlayer)
                    .format(Localization.Command.Ban::getNullTime)
                    .sendBuilt();
            return;
        }

        ban(fPlayer, target, time, reason);
    }

    public void ban(FPlayer fPlayer, String target, long time, String reason) {
        if (checkModulePredicates(fPlayer)) return;

        threadManager.runDatabase(database -> {
            FPlayer fTarget = database.getFPlayer(target);
            if (fTarget.isUnknown()) {
                builder(fPlayer)
                        .format(Localization.Command.Ban::getNullPlayer)
                        .sendBuilt();
                return;
            }

            long databaseTime = time != -1 ? time + System.currentTimeMillis() : -1;

            Moderation ban = database.insertModeration(fTarget, databaseTime, reason, fPlayer.getId(), Moderation.Type.BAN);
            if (ban == null) return;

            kick(fPlayer, fTarget, ban);

            builder(fPlayer)
                    .range(command.getRange())
                    .destination(command.getDestination())
                    .tag(MessageTag.COMMAND_BAN)
                    .format(replaceTarget(fTarget.getName(), time))
                    .message(s -> getTypeLocalization(s, time).getReasons().getConstant(reason))
                    .proxy(output -> {
                        output.writeUTF(gson.toJson(fTarget));
                        output.writeUTF(gson.toJson(ban));
                    })
                    .integration(s -> s
                            .replace("<reason>", getTypeLocalization(resolveLocalization(), time).getReasons().getConstant(reason))
                            .replace("<target>", fTarget.getName())
                            .replace("<time>", timeUtil.format(FPlayer.UNKNOWN, time))
                    )
                    .sound(getSound())
                    .sendBuilt();
        });
    }

    public Localization.Command.Ban.Type getTypeLocalization(Localization.Command.Ban message, long time) {
        return time == -1 ? message.getPermanent() : message.getTemporarily();
    }

    public BiFunction<FPlayer, Localization.Command.Ban, String> replaceTarget(String target, long time) {
        return (fReceiver, message) -> timeUtil.format(fReceiver, time, getTypeLocalization(message, time).getGlobal().replace("<target>", target));
    }

    public void kick(FEntity fModerator, FPlayer fTarget, Moderation ban) {
        if (checkModulePredicates(fModerator)) return;
        if (fModerator == null) return;

        threadManager.runAsync(database -> {
            Localization.Command.Ban localization = resolveLocalization(fTarget);

            String formatPlayer = timeUtil.format(fTarget, ban.getRemainingTime(), getTypeLocalization(localization, ban.getTime()).getPlayer()
                    .replace("<message>", getTypeLocalization(localization, ban.getTime()).getReasons().getConstant(ban.getReason()))
                    .replace("<moderator>", fModerator.getName())
            );

            fPlayerManager.kick(fTarget, componentUtil.builder(fModerator, fTarget, formatPlayer).build());
        });
    }

    public void checkJoin(UUID uuid, Object channel) {
        if (!isEnable()) return;

        try {
            FPlayer fPlayer = database.getFPlayer(uuid);

            for (Moderation ban : database.getValidModerations(fPlayer, Moderation.Type.BAN)) {
                FPlayer fModerator = database.getFPlayer(ban.getModerator());

                Localization.Command.Ban.Type localization = getTypeLocalization(resolveLocalization(fPlayer), ban.getTime());

                String formatPlayer = localization.getPlayer();
                formatPlayer = timeUtil.format(fPlayer, ban.getRemainingTime(), formatPlayer
                        .replace("<message>", getTypeLocalization(resolveLocalization(), ban.getTime()).getReasons().getConstant(ban.getReason()))
                        .replace("<moderator>", fModerator.getName())
                );

                Component reason = componentUtil.builder(fModerator, fPlayer, formatPlayer).build();
                packetEventsUtil.sendPacket(channel, new WrapperLoginServerDisconnect(reason));

                if (!command.isShowConnectionAttempts()) return;

                builder(fPlayer)
                        .filter(filter -> permissionUtil.has(filter, getModulePermission()))
                        .format((fReceiver, message) -> timeUtil.format(fReceiver, ban.getRemainingTime(), getTypeLocalization(message, ban.getTime()).getConnectionAttempt()
                                .replace("<message>", getTypeLocalization(resolveLocalization(), ban.getTime()).getReasons().getConstant(ban.getReason()))
                                .replace("<target>", fPlayer.getName())
                        ))
                        .sendBuilt();
            }

        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        listenerManager.register(BanPacketListener.class);

        getCommand().getAliases().forEach(commandUtil::unregister);

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}
