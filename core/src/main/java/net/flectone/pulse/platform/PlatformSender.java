package net.flectone.pulse.platform;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBossBar;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import com.google.inject.Inject;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.Destination;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.BrandPacketSerializer;
import net.flectone.pulse.util.PacketEventsUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;

import java.util.UUID;

public abstract class PlatformSender {

    public abstract void sendMessage(FPlayer fPlayer, Component component);

    public abstract void sendTitle(FPlayer fPlayer, Title.Times times, Component title, Component subTitle);

    public abstract void sendActionBar(FPlayer fPlayer, Component component);

    public abstract void sendPlayerListFooter(FPlayer fPlayer, Component component);

    public abstract void sendPlayerListHeader(FPlayer fPlayer, Component component);

    public void send(Destination destination, FPlayer fPlayer, Component component) {
        if (!Component.IS_NOT_EMPTY.test(component)) return;

        switch (destination.getType()) {
            case TITLE -> sendTitle(fPlayer, destination.getTimes(), component, Component.empty());
            case SUBTITLE -> sendTitle(fPlayer, destination.getTimes(), Component.empty(), component);
            case ACTION_BAR -> sendActionBar(fPlayer, component);
            case BOSS_BAR -> sendBoosBar(fPlayer, component, destination);
            case TAB_HEADER -> sendPlayerListHeader(fPlayer, component);
            case TAB_FOOTER -> sendPlayerListFooter(fPlayer, component);
            case BRAND -> sendBrand(fPlayer, component);
            default -> sendMessage(fPlayer, component);
        }
    }

    @Inject
    private PacketEventsUtil packetEventsUtil;

    @Inject
    private BrandPacketSerializer brandPacketSerializer;

    @Inject
    private ThreadManager threadManager;

    public void sendBoosBar(FPlayer fPlayer, Component component, Destination destination) {
        UUID bossBarUUID = UUID.randomUUID();

        WrapperPlayServerBossBar addWrapper = new WrapperPlayServerBossBar(bossBarUUID, WrapperPlayServerBossBar.Action.ADD);

        addWrapper.setTitle(component);
        addWrapper.setHealth(destination.getHealth());
        addWrapper.setOverlay(destination.getOverlay());
        addWrapper.setColor(destination.getColor());
        addWrapper.setFlags(destination.getFlags());

        packetEventsUtil.sendPacket(fPlayer, addWrapper);

        threadManager.runAsyncLater(() -> {
            WrapperPlayServerBossBar removeWrapper = new WrapperPlayServerBossBar(bossBarUUID, WrapperPlayServerBossBar.Action.REMOVE);
            packetEventsUtil.sendPacket(fPlayer, removeWrapper);

        }, destination.getDuration());
    }

    public void sendBrand(FPlayer fPlayer, Component component) {
        String message = LegacyComponentSerializer.legacySection().serialize(component);

        byte[] data = brandPacketSerializer.serialize(message + "§r");
        if (data == null) return;

        packetEventsUtil.sendPacket(fPlayer, new WrapperPlayServerPluginMessage(BrandPacketSerializer.MINECRAFT_BRAND, data));
    }
}
