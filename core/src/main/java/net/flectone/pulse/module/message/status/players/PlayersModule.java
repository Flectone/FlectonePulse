package net.flectone.pulse.module.message.status.players;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerDisconnect;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.adapter.PlatformServerAdapter;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.sender.PacketSender;
import net.kyori.adventure.text.Component;

import java.util.List;

@Singleton
public class PlayersModule extends AbstractModuleMessage<Localization.Message.Status.Players> {

    @Getter private final Message.Status.Players message;
    private final Permission.Message.Status.Players permission;

    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final PlatformServerAdapter platformServerAdapter;
    private final MessagePipeline messagePipeline;
    private final PacketSender packetSender;

    @Inject
    public PlayersModule(FileResolver fileResolver,
                         FPlayerService fPlayerService,
                         PermissionChecker permissionChecker,
                         PlatformServerAdapter platformServerAdapter,
                         MessagePipeline messagePipeline,
                         PacketSender packetSender) {
        super(module -> module.getMessage().getStatus().getPlayers());

        this.fPlayerService = fPlayerService;
        this.permissionChecker = permissionChecker;
        this.platformServerAdapter = platformServerAdapter;
        this.messagePipeline = messagePipeline;
        this.packetSender = packetSender;

        message = fileResolver.getMessage().getStatus().getPlayers();
        permission = fileResolver.getPermission().getMessage().getStatus().getPlayers();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);
        registerPermission(permission.getBypass());
    }

    public boolean isKicked(UserProfile userProfile) {
        if (!isEnable()) return false;
        if (!message.isControl()) return false;

        FPlayer fPlayer = fPlayerService.getFPlayer(userProfile.getUUID());

        if (checkModulePredicates(fPlayer)) return false;
        if (permissionChecker.check(fPlayer, permission.getBypass())) return false;

        int online = platformServerAdapter.getOnlinePlayerCount();
        if (online < message.getMax()) return false;

        fPlayerService.loadSettings(fPlayer);
        fPlayerService.loadColors(fPlayer);

        String message = resolveLocalization(fPlayer).getFull();

        Component reason = messagePipeline.builder(fPlayer, message).build();

        packetSender.send(userProfile.getUUID(), new WrapperLoginServerDisconnect(reason));
        return true;
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public List<Localization.Message.Status.Players.Sample> getSamples(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return null;

        return resolveLocalization(fPlayer).getSamples();
    }
}
