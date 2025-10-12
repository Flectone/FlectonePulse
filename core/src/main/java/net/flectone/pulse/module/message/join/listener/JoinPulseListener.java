package net.flectone.pulse.module.message.join.listener;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class JoinPulseListener implements PulseListener {

    private final PacketProvider packetProvider;
    private final JoinModule joinModule;

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.getPlayer();
        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_20_2)) {
            // delay for vanish plugins and newer versions
            joinModule.sendLater(fPlayer);
        } else {
            joinModule.send(fPlayer, false);
        }
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        if (translatableComponent == null) return;

        String translationKey = translatableComponent.key();
        if (!translationKey.equals("multiplayer.player.joined") && !translationKey.equals("multiplayer.player.joined.renamed")) return;

        event.setCancelled(true);
    }
}
