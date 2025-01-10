package net.flectone.pulse.module.message.status.players;

import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerDisconnect;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ListenerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.status.players.listener.PlayersPacketListener;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.PacketEventsUtil;
import net.flectone.pulse.util.PermissionUtil;
import net.flectone.pulse.util.ServerUtil;
import net.kyori.adventure.text.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Singleton
public class PlayersModule extends AbstractModuleMessage<Localization.Message.Status.Players> {

    @Getter private final Message.Status.Players message;
    private final Permission.Message.Status.Players permission;

    private final ListenerManager listenerManager;
    private final PermissionUtil permissionUtil;
    private final ServerUtil serverUtil;
    private final ComponentUtil componentUtil;
    private final PacketEventsUtil packetEventsUtil;
    private final Database database;
    private final FLogger fLogger;

    @Inject
    public PlayersModule(FileManager fileManager,
                         ListenerManager listenerManager,
                         PermissionUtil permissionUtil,
                         ServerUtil serverUtil,
                         ComponentUtil componentUtil,
                         PacketEventsUtil packetEventsUtil,
                         Database database,
                         FLogger fLogger) {
        super(module -> module.getMessage().getStatus().getPlayers());

        this.listenerManager = listenerManager;
        this.permissionUtil = permissionUtil;
        this.serverUtil = serverUtil;
        this.componentUtil = componentUtil;
        this.packetEventsUtil = packetEventsUtil;
        this.database = database;
        this.fLogger = fLogger;

        message = fileManager.getMessage().getStatus().getPlayers();
        permission = fileManager.getPermission().getMessage().getStatus().getPlayers();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
        registerPermission(permission.getBypass());

        listenerManager.register(PlayersPacketListener.class);
    }

    public void check(UUID uuid, Object channel) {
        if (!message.isControl()) return;

        FPlayer fPlayer = FPlayer.UNKNOWN;

        try {
            fPlayer = database.getFPlayer(uuid);
        } catch (SQLException e) {
            fLogger.warning(e);
        }

        if (checkModulePredicates(fPlayer)) return;
        if (permissionUtil.has(fPlayer, permission.getBypass())) return;

        int online = serverUtil.getOnlineCount();
        if (online < message.getMax()) return;

        String message = resolveLocalization(fPlayer).getFull();

        Component reason = componentUtil.builder(fPlayer, message).build();

        packetEventsUtil.sendPacket(channel, new WrapperLoginServerDisconnect(reason));
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    public List<Localization.Message.Status.Players.Sample> getSamples(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return null;

        return resolveLocalization(fPlayer).getSamples();
    }
}
