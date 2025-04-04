package net.flectone.pulse.message;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.model.*;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.util.PacketEventsUtil;
import net.flectone.pulse.util.logging.FLogger;
import net.flectone.pulse.util.serializer.BrandPacketSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;

public abstract class MessageSender {

    private final BrandPacketSerializer packetSerializer;
    private final TaskScheduler taskScheduler;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PacketEventsUtil packetEventsUtil;
    private final FLogger fLogger;

    public MessageSender(TaskScheduler taskScheduler,
                         PlatformPlayerAdapter platformPlayerAdapter,
                         BrandPacketSerializer packetSerializer,
                         PacketEventsUtil packetEventsUtil,
                         FLogger fLogger) {
        this.taskScheduler = taskScheduler;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.packetSerializer = packetSerializer;
        this.packetEventsUtil = packetEventsUtil;
        this.fLogger = fLogger;
    }

    public void send(FPlayer fPlayer, Component component, Component subcomponent, Destination destination) {
        if (!Component.IS_NOT_EMPTY.test(component)) return;

        switch (destination.getType()) {
            case TITLE -> sendTitle(fPlayer, component, subcomponent, destination.getTimes());
            case SUBTITLE -> sendTitle(fPlayer, subcomponent, component, destination.getTimes());
            case ACTION_BAR -> sendActionBar(fPlayer, component, destination.getTimes().stayTicks());
            case BOSS_BAR -> sendBoosBar(fPlayer, component, destination.getBossBar());
            case TAB_HEADER -> sendPlayerListHeaderAndFooter(fPlayer, component, platformPlayerAdapter.getPlayerListFooter(fPlayer));
            case TAB_FOOTER -> sendPlayerListHeaderAndFooter(fPlayer, platformPlayerAdapter.getPlayerListHeader(fPlayer), component);
            case TOAST -> sendToast(fPlayer, component, destination.getToast());
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

    public void sendTitle(FPlayer fPlayer, Component title, Component subTitle, Times times) {
        User user = packetEventsUtil.getUser(fPlayer);
        if (user == null) return;

        user.sendTitle(title, subTitle, times.fadeInTicks(), times.stayTicks(), times.fadeOutTicks());
    }

    public void sendActionBar(FPlayer fPlayer, Component component) {
        sendActionBar(fPlayer, component, 0);
    }

    public void sendActionBar(FPlayer fPlayer, Component component, int stayTicks) {
        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_19)) {
            packetEventsUtil.sendPacket(fPlayer, new WrapperPlayServerSystemChatMessage(true, component));
        } else {
            packetEventsUtil.sendPacket(fPlayer, new WrapperPlayServerActionBar(component));
        }

        // cannot set stay ticks for action bar, so
        if (stayTicks <= 30) return;

        int remainingTicks = stayTicks - 30;
        int delay = Math.min(30, remainingTicks);

        taskScheduler.runAsyncLater(() -> sendActionBar(fPlayer, component, remainingTicks), delay);
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

        taskScheduler.runAsyncLater(() -> {
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

    public abstract void sendToast(FPlayer fPlayer, Component title, Toast toast);
}
