package net.flectone.pulse.module.command.maintenance;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerDisconnect;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ListenerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.maintenance.listener.MaintenancePacketListener;
import net.flectone.pulse.util.*;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.UUID;

public abstract class MaintenanceModule extends AbstractModuleCommand<Localization.Command.Maintenance> {

    @Getter
    private final Command.Maintenance command;
    @Getter
    private final Permission.Command.Maintenance permission;

    private final FileManager fileManager;
    private final FPlayerManager fPlayerManager;
    private final PermissionUtil permissionUtil;
    private final ListenerManager listenerManager;
    private final Database database;
    private final Path iconPath;
    private final FileUtil fileUtil;
    private final ComponentUtil componentUtil;
    private final CommandUtil commandUtil;
    private final PacketEventsUtil packetEventsUtil;
    private final FLogger fLogger;

    private String icon;

    public MaintenanceModule(FileManager fileManager,
                             FPlayerManager fPlayerManager,
                             PermissionUtil permissionUtil,
                             ListenerManager listenerManager,
                             Database database,
                             Path pluginPath,
                             FileUtil fileUtil,
                             CommandUtil commandUtil,
                             ComponentUtil componentUtil,
                             PacketEventsUtil packetEventsUtil,
                             FLogger fLogger) {
        super(module -> module.getCommand().getMaintenance(), null);

        this.fileManager = fileManager;
        this.fPlayerManager = fPlayerManager;
        this.permissionUtil = permissionUtil;
        this.commandUtil = commandUtil;
        this.listenerManager = listenerManager;
        this.database = database;
        this.iconPath = Paths.get(pluginPath.toString(), "images");
        this.fileUtil = fileUtil;
        this.componentUtil = componentUtil;
        this.packetEventsUtil = packetEventsUtil;
        this.fLogger = fLogger;

        command = fileManager.getCommand().getMaintenance();
        permission = fileManager.getPermission().getCommand().getMaintenance();

        addPredicate(this::checkCooldown);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        boolean turned = commandUtil.getBoolean(0, arguments);
        if (turned && command.isTurnedOn()) {
            builder(fPlayer)
                    .format(Localization.Command.Maintenance::getAlready)
                    .sendBuilt();
            return;
        }

        if (!turned && !command.isTurnedOn()) {
            builder(fPlayer)
                    .format(Localization.Command.Maintenance::getNot)
                    .sendBuilt();
            return;
        }

        builder(fPlayer)
                .destination(command.getDestination())
                .format(s -> turned ? s.getFormatTrue() : s.getFormatFalse())
                .sendBuilt();

        command.setTurnedOn(turned);
        fileManager.save();

        if (!turned) return;

        fPlayerManager.getFPlayers()
                .stream()
                .filter(FPlayer::isOnline)
                .filter(filter -> !permissionUtil.has(filter, permission.getJoin()))
                .forEach(fReceiver -> {
                    Component component = componentUtil.builder(fPlayer, fReceiver, resolveLocalization(fReceiver).getKick()).build();
                    fPlayerManager.kick(fPlayer, component);
                });
    }

    public void sendStatus(User user) {
        if (!isEnable()) return;
        if (!command.isTurnedOn()) return;

        FPlayer fPlayer = FPlayer.UNKNOWN;

        try {
            fPlayer = database.getFPlayer(user.getAddress().getAddress());
            database.setColors(fPlayer);
        } catch (SQLException e) {
            fLogger.warning(e);
        }

        JsonObject responseJson = new JsonObject();

        Localization.Command.Maintenance localizationMaintenance = resolveLocalization(fPlayer);

        responseJson.add("version", getVersionJson(localizationMaintenance.getServerVersion()));
        responseJson.add("players", getPlayersJson());

        responseJson.add("description", componentUtil.builder(fPlayer, localizationMaintenance.getServerDescription()).serializeToTree());
        responseJson.addProperty("favicon", "data:image/png;base64," + (icon == null ? "" : icon));
        responseJson.addProperty("enforcesSecureChat", false);

        WrapperStatusServerResponse wrapperStatusServerResponse = new WrapperStatusServerResponse(responseJson);
        user.sendPacket(wrapperStatusServerResponse);
    }

    private JsonElement getVersionJson(String message) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", message);
        jsonObject.addProperty("protocol", -1);

        return jsonObject;
    }

    private JsonElement getPlayersJson() {
        JsonObject playersJson = new JsonObject();

        playersJson.addProperty("max", -1);
        playersJson.addProperty("online", -1);

        playersJson.add("sample", new JsonArray());

        return playersJson;
    }

    public void checkJoin(UUID uuid, Object channel) {
        if (!isEnable()) return;
        if (!command.isTurnedOn()) return;

        String messageKick = resolveLocalization().getKick();

        try {
            FPlayer fPlayer = database.getFPlayer(uuid);
            if (permissionUtil.has(fPlayer, permission.getJoin())) return;

            Component reason = componentUtil.builder(fPlayer, messageKick).build();
            packetEventsUtil.sendPacket(channel, new WrapperLoginServerDisconnect(reason));

        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        registerPermission(permission.getJoin());

        listenerManager.register(MaintenancePacketListener.class);

        File file = new File(iconPath.toString() + File.separator + "maintenance.png");

        if (!file.exists()) {
            fileUtil.saveResource("images" + File.separator + "maintenance.png");
        }

        icon = fileUtil.convertIcon(file);

        getCommand().getAliases().forEach(commandUtil::unregister);

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }

}
