package net.flectone.pulse.platform;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBossBar;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.*;
import net.flectone.pulse.util.BrandPacketSerializer;
import net.flectone.pulse.util.PacketEventsUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;

public abstract class MessageSender {

    private final BrandPacketSerializer packetSerializer;
    private final ThreadManager threadManager;
    private final FPlayerManager fPlayerManager;
    private final PacketEventsUtil packetEventsUtil;
    private final FLogger fLogger;

    public MessageSender(ThreadManager threadManager,
                         FPlayerManager fPlayerManager,
                         BrandPacketSerializer packetSerializer,
                         PacketEventsUtil packetEventsUtil,
                         FLogger fLogger) {
        this.threadManager = threadManager;
        this.fPlayerManager = fPlayerManager;
        this.packetSerializer = packetSerializer;
        this.packetEventsUtil = packetEventsUtil;
        this.fLogger = fLogger;
    }

    public void send(Destination destination, FPlayer fPlayer, Component component) {
        if (!Component.IS_NOT_EMPTY.test(component)) return;

        switch (destination.getType()) {
            case TITLE -> sendTitle(fPlayer, destination.getTimes(), component, Component.empty());
            case SUBTITLE -> sendTitle(fPlayer, destination.getTimes(), Component.empty(), component);
            case ACTION_BAR -> sendActionBar(fPlayer, component);
            case BOSS_BAR -> sendBoosBar(fPlayer, component, destination.getBossBar());
            case TAB_HEADER -> sendPlayerListHeaderAndFooter(fPlayer, component, fPlayerManager.getPlayerListFooter(fPlayer));
            case TAB_FOOTER -> sendPlayerListHeaderAndFooter(fPlayer, fPlayerManager.getPlayerListHeader(fPlayer), component);
            case TOAST -> sendToast(fPlayer, component, subcomponent, destination.getToast());
            case BRAND -> sendBrand(fPlayer, component);
            default -> sendMessage(fPlayer, component);
        }
    }

    public void sendMessage(FPlayer fPlayer, Component component) {

        if (fPlayer.isUnknown()) {
            fLogger.info(component);
            return;
        }

        User user = packetEventsUtil.getUser(fPlayer);
        if (user == null) return;

        user.sendMessage(component);
    }

    public void sendTitle(FPlayer fPlayer, Times times, Component title, Component subTitle) {
        User user = packetEventsUtil.getUser(fPlayer);
        if (user == null) return;

        user.sendTitle(title, subTitle, times.fadeInTicks(), times.stayTicks(), times.fadeOutTicks());
    }

    public void sendActionBar(FPlayer fPlayer, Component component) {;
        packetEventsUtil.sendPacket(fPlayer, new WrapperPlayServerSystemChatMessage(true, component));
    }

    public void sendPlayerListHeaderAndFooter(FPlayer fPlayer, Component header, Component footer) {
        packetEventsUtil.sendPacket(fPlayer, new WrapperPlayServerPlayerListHeaderAndFooter(header, footer));
    }

    public void sendBoosBar(FPlayer fPlayer, Component component, BossBar bossBar) {
        UUID bossBarUUID = UUID.randomUUID();

        WrapperPlayServerBossBar addWrapper = new WrapperPlayServerBossBar(bossBarUUID, WrapperPlayServerBossBar.Action.ADD);

        addWrapper.setTitle(component);
        addWrapper.setHealth(bossBar.getHealth());
        addWrapper.setOverlay(bossBar.getOverlay());
        addWrapper.setColor(bossBar.getColor());
        addWrapper.setFlags(bossBar.getFlags());

        packetEventsUtil.sendPacket(fPlayer, addWrapper);

        threadManager.runAsyncLater(() -> {
            WrapperPlayServerBossBar removeWrapper = new WrapperPlayServerBossBar(bossBarUUID, WrapperPlayServerBossBar.Action.REMOVE);
            packetEventsUtil.sendPacket(fPlayer, removeWrapper);

        }, bossBar.getDuration());
    }

    public void sendBrand(FPlayer fPlayer, Component component) {
        String message = LegacyComponentSerializer.legacySection().serialize(component) + "§r";

        packetEventsUtil.sendPacket(fPlayer,
                new WrapperPlayServerPluginMessage(BrandPacketSerializer.MINECRAFT_BRAND, packetSerializer.serialize(message))
        );
    }

    public abstract void sendToast(FPlayer fPlayer, Component component, Toast toast);

}
