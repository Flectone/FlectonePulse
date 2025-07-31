package net.flectone.pulse.module.message.death.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeathCombatEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class DeathPacketListener implements PacketListener {

    @Inject
    public DeathPacketListener() {
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() != PacketType.Play.Server.DEATH_COMBAT_EVENT) return;

        WrapperPlayServerDeathCombatEvent wrapperPlayServerDeathCombatEvent = new WrapperPlayServerDeathCombatEvent(event);
        if (wrapperPlayServerDeathCombatEvent.getDeathMessage() instanceof TranslatableComponent) {
            event.setCancelled(true);
        }
    }
}
