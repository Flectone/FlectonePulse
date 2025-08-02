package net.flectone.pulse.module.message.join.listener;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.platform.provider.PacketProvider;

@Singleton
public class JoinPulseListener implements PulseListener {

    private final PacketProvider packetProvider;
    private final JoinModule joinModule;

    @Inject
    public JoinPulseListener(PacketProvider packetProvider,
                             JoinModule joinModule) {
        this.packetProvider = packetProvider;
        this.joinModule = joinModule;
    }

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.getPlayer();
        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_20_2)) {
            // delay for vanish plugins and newer versions
            joinModule.sendLater(fPlayer);
        } else {
            joinModule.send(fPlayer);
        }
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(TranslatableMessageReceiveEvent event) {
        if (event.getKey() != MinecraftTranslationKey.MULTIPLAYER_PLAYER_JOINED) return;

        event.cancelPacket();
    }
}
