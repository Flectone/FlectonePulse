package net.flectone.pulse.platform.render;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBossBar;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.BossBar;
import net.flectone.pulse.platform.sender.PacketSender;
import net.kyori.adventure.text.Component;

import java.util.UUID;

/**
 * Renders boss bar displays to players with automatic cleanup.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * BossBarRender bossBarRender = flectonePulse.get(BossBarRender.class);
 *
 * BossBar bossBar = new BossBar(
 *     0.5f, // 50% health
 *     Color.BLUE,
 *     Overlay.PROGRESS,
 *     Set.of(Flag.DARKEN_SKY),
 *     100 // 5 seconds
 * );
 *
 * bossBarRender.render(player, Component.text("Boss Fight!"), bossBar);
 * }</pre>
 *
 * @since 1.7.0
 * @author TheFaser
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BossBarRender {

    private final PacketSender packetSender;
    private final TaskScheduler taskScheduler;

    /**
     * Renders a boss bar to a player with automatic removal.
     *
     * @param fPlayer the player to receive the boss bar
     * @param component the title component to display
     * @param bossBar the boss bar configuration
     */
    public void render(FPlayer fPlayer, Component component, BossBar bossBar) {
        UUID bossBarUUID = UUID.randomUUID();

        WrapperPlayServerBossBar addWrapper = new WrapperPlayServerBossBar(bossBarUUID, WrapperPlayServerBossBar.Action.ADD);

        addWrapper.setTitle(component);
        addWrapper.setHealth(bossBar.health());
        addWrapper.setOverlay(bossBar.overlay());
        addWrapper.setColor(bossBar.color());
        addWrapper.setFlags(bossBar.flags());

        packetSender.send(fPlayer, addWrapper);

        taskScheduler.runAsyncLater(() -> {
            WrapperPlayServerBossBar removeWrapper = new WrapperPlayServerBossBar(bossBarUUID, WrapperPlayServerBossBar.Action.REMOVE);
            packetSender.send(fPlayer, removeWrapper);

        }, bossBar.duration());
    }

}
