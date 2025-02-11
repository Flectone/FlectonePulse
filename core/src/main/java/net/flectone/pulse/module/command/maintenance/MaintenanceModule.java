package net.flectone.pulse.module.command.maintenance;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerDisconnect;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.flectone.pulse.database.dao.ColorsDAO;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
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

public abstract class MaintenanceModule extends AbstractModuleCommand<Localization.Command.Maintenance> {

    @Getter private final Command.Maintenance command;
    @Getter private final Permission.Command.Maintenance permission;

    private final FileManager fileManager;
    private final FPlayerManager fPlayerManager;
    private final PermissionUtil permissionUtil;
    private final ListenerManager listenerManager;
    private final FPlayerDAO fPlayerDAO;
    private final ColorsDAO colorsDAO;
    private final Path iconPath;
    private final FileUtil fileUtil;
    private final ComponentUtil componentUtil;
    private final CommandUtil commandUtil;
    private final PacketEventsUtil packetEventsUtil;

    private String icon;

    public MaintenanceModule(FileManager fileManager,
                             FPlayerManager fPlayerManager,
                             PermissionUtil permissionUtil,
                             ListenerManager listenerManager,
                             FPlayerDAO fPlayerDAO,
                             ColorsDAO colorsDAO,
                             Path projectPath,
                             FileUtil fileUtil,
                             CommandUtil commandUtil,
                             ComponentUtil componentUtil,
                             PacketEventsUtil packetEventsUtil) {
        super(module -> module.getCommand().getMaintenance(), null);

        this.fileManager = fileManager;
        this.fPlayerManager = fPlayerManager;
        this.permissionUtil = permissionUtil;
        this.commandUtil = commandUtil;
        this.listenerManager = listenerManager;
        this.fPlayerDAO = fPlayerDAO;
        this.colorsDAO = colorsDAO;
        this.iconPath = projectPath.resolve("images");
        this.fileUtil = fileUtil;
        this.componentUtil = componentUtil;
        this.packetEventsUtil = packetEventsUtil;

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

        if (turned) {
            kickOnlinePlayers(fPlayer);
        }
    }

    public void sendStatus(User user) {
        if (!isEnable()) return;
        if (!command.isTurnedOn()) return;

        FPlayer fPlayer = fPlayerDAO.getFPlayer(user.getAddress().getAddress());
        colorsDAO.setFPlayerColors(fPlayer);

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

    public boolean isKicked(UserProfile userProfile) {
        if (!isEnable()) return false;
        if (!command.isTurnedOn()) return false;

        String messageKick = resolveLocalization().getKick();

        FPlayer fPlayer = fPlayerDAO.getFPlayer(userProfile.getUUID());
        if (permissionUtil.has(fPlayer, permission.getJoin())) return false;

        Component reason = componentUtil.builder(fPlayer, messageKick).build();
        packetEventsUtil.sendPacket(userProfile.getUUID(), new WrapperLoginServerDisconnect(reason));
        return true;
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

        if (command.isTurnedOn()) {
            kickOnlinePlayers(FPlayer.UNKNOWN);
        }
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
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

    private void kickOnlinePlayers(FPlayer fSender) {
        fPlayerManager.getFPlayers()
                .stream()
                .filter(FPlayer::isOnline)
                .filter(filter -> !permissionUtil.has(filter, permission.getJoin()))
                .forEach(fReceiver -> {
                    Component component = componentUtil.builder(fSender, fReceiver, resolveLocalization(fReceiver).getKick()).build();
                    fPlayerManager.kick(fReceiver, component);
                });
    }
}
