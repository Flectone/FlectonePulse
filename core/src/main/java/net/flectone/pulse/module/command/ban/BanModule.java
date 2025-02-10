package net.flectone.pulse.module.command.ban;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
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
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.*;
import net.kyori.adventure.text.Component;

import java.sql.SQLException;
import java.util.function.BiFunction;

public abstract class BanModule extends AbstractModuleCommand<Localization.Command.Ban> {

    @Getter private final Command.Ban command;
    @Getter private final Permission.Command.Ban permission;

    private final Database database;
    private final ThreadManager threadManager;
    private final FPlayerManager fPlayerManager;
    private final PermissionUtil permissionUtil;
    private final CommandUtil commandUtil;
    private final ComponentUtil componentUtil;
    private final PacketEventsUtil packetEventsUtil;
    private final ModerationUtil moderationUtil;
    private final FLogger fLogger;
    private final Gson gson;

    public BanModule(Database database,
                     FileManager fileManager,
                     FPlayerManager fPlayerManager,
                     PermissionUtil permissionUtil,
                     ThreadManager threadManager,
                     CommandUtil commandUtil,
                     ComponentUtil componentUtil,
                     PacketEventsUtil packetEventsUtil,
                     ModerationUtil moderationUtil,
                     FLogger fLogger,
                     Gson gson) {
        super(localization -> localization.getCommand().getBan(), fPlayer -> fPlayer.is(FPlayer.Setting.BAN));

        this.database = database;
        this.threadManager = threadManager;
        this.fPlayerManager = fPlayerManager;
        this.permissionUtil = permissionUtil;
        this.commandUtil = commandUtil;
        this.componentUtil = componentUtil;
        this.packetEventsUtil = packetEventsUtil;
        this.moderationUtil = moderationUtil;
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

            builder(fTarget)
                    .range(command.getRange())
                    .destination(command.getDestination())
                    .tag(MessageTag.COMMAND_BAN)
                    .format(buildFormat(ban))
                    .proxy(output -> {
                        output.writeUTF(gson.toJson(fPlayer));
                        output.writeUTF(gson.toJson(ban));
                    })
                    .integration(s -> moderationUtil.replacePlaceholders(s, FPlayer.UNKNOWN, ban))
                    .sound(getSound())
                    .sendBuilt();
        });
    }

    public BiFunction<FPlayer, Localization.Command.Ban, String> buildFormat(Moderation ban) {
        return (fReceiver, message) -> {
            String format = message.getServer();

            return moderationUtil.replacePlaceholders(format, fReceiver, ban);
        };
    }

    public void kick(FEntity fModerator, FPlayer fTarget, Moderation ban) {
        if (checkModulePredicates(fModerator)) return;
        if (fModerator == null) return;

        threadManager.runAsync(database -> {
            Localization.Command.Ban localization = resolveLocalization(fTarget);

            String formatPlayer = localization.getPerson();
            formatPlayer = moderationUtil.replacePlaceholders(formatPlayer, fTarget, ban);

            fPlayerManager.kick(fTarget, componentUtil.builder(fModerator, fTarget, formatPlayer).build());
        });
    }

    public boolean isKicked(UserProfile userProfile) {
        if (!isEnable()) return false;

        try {
            FPlayer fPlayer = database.getFPlayer(userProfile.getUUID());

            for (Moderation ban : database.getValidModerations(fPlayer, Moderation.Type.BAN)) {
                FPlayer fModerator = database.getFPlayer(ban.getModerator());

                Localization.Command.Ban localization = resolveLocalization();

                String formatPlayer = localization.getPerson();
                formatPlayer =  moderationUtil.replacePlaceholders(formatPlayer, fPlayer, ban);

                Component reason = componentUtil.builder(fModerator, fPlayer, formatPlayer).build();
                packetEventsUtil.sendPacket(userProfile.getUUID(), new WrapperLoginServerDisconnect(reason));

                if (command.isShowConnectionAttempts()) {
                    builder(fPlayer)
                            .range(Range.SERVER)
                            .filter(filter -> permissionUtil.has(filter, getModulePermission()))
                            .format((fReceiver, message) -> {
                                String format = message.getConnectionAttempt();
                                return moderationUtil.replacePlaceholders(format, fReceiver, ban);
                            })
                            .sendBuilt();
                }

                return true;
            }

        } catch (SQLException e) {
            fLogger.warning(e);
        }

        return false;
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
