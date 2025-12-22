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

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BossBarRender {

    private final PacketSender packetSender;
    private final TaskScheduler taskScheduler;

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
