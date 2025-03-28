package net.flectone.pulse.module.message.status.players;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerDisconnect;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.PacketEventsUtil;
import net.flectone.pulse.util.PermissionUtil;
import net.flectone.pulse.util.ServerUtil;
import net.kyori.adventure.text.Component;

import java.util.List;

@Singleton
public class PlayersModule extends AbstractModuleMessage<Localization.Message.Status.Players> {

    @Getter private final Message.Status.Players message;
    private final Permission.Message.Status.Players permission;

    private final FPlayerService fPlayerService;
    private final PermissionUtil permissionUtil;
    private final ServerUtil serverUtil;
    private final ComponentUtil componentUtil;
    private final PacketEventsUtil packetEventsUtil;

    @Inject
    public PlayersModule(FileManager fileManager,
                         FPlayerService fPlayerService,
                         PermissionUtil permissionUtil,
                         ServerUtil serverUtil,
                         ComponentUtil componentUtil,
                         PacketEventsUtil packetEventsUtil) {
        super(module -> module.getMessage().getStatus().getPlayers());

        this.fPlayerService = fPlayerService;
        this.permissionUtil = permissionUtil;
        this.serverUtil = serverUtil;
        this.componentUtil = componentUtil;
        this.packetEventsUtil = packetEventsUtil;

        message = fileManager.getMessage().getStatus().getPlayers();
        permission = fileManager.getPermission().getMessage().getStatus().getPlayers();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
        registerPermission(permission.getBypass());
    }

    public boolean isKicked(UserProfile userProfile) {
        if (!isEnable()) return false;
        if (!message.isControl()) return false;

        FPlayer fPlayer = fPlayerService.getFPlayer(userProfile.getUUID());

        if (checkModulePredicates(fPlayer)) return false;
        if (permissionUtil.has(fPlayer, permission.getBypass())) return false;

        int online = serverUtil.getOnlineCount();
        if (online < message.getMax()) return false;

        String message = resolveLocalization(fPlayer).getFull();

        Component reason = componentUtil.builder(fPlayer, message).build();

        packetEventsUtil.sendPacket(userProfile.getUUID(), new WrapperLoginServerDisconnect(reason));
        return true;
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
