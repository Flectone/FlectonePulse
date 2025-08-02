package net.flectone.pulse.model.event.message;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import lombok.Getter;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.kyori.adventure.text.TranslatableComponent;

@Getter
public class TranslatableMessageReceiveEvent extends Event {

    private final FPlayer fPlayer;
    private final MinecraftTranslationKey key;
    private final TranslatableComponent component;
    private final PacketSendEvent packetEvent;

    public TranslatableMessageReceiveEvent(FPlayer fPlayer,
                                           MinecraftTranslationKey key,
                                           TranslatableComponent component,
                                           PacketSendEvent packetEvent) {
        this.fPlayer = fPlayer;
        this.key = key;
        this.component = component;
        this.packetEvent = packetEvent;
    }

    public void cancelPacket() {
        packetEvent.setCancelled(true);
    }

}
