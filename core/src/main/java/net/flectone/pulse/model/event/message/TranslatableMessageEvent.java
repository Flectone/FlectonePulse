package net.flectone.pulse.model.event.message;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.player.User;
import lombok.Getter;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.UUID;

@Getter
public class TranslatableMessageEvent extends Event {

    private final MinecraftTranslationKey key;
    private final TranslatableComponent component;
    private final PacketSendEvent packetEvent;

    public TranslatableMessageEvent(MinecraftTranslationKey key, TranslatableComponent component, PacketSendEvent packetEvent) {
        super(Type.MESSAGE);

        this.key = key;
        this.component = component;
        this.packetEvent = packetEvent;
    }

    public User getUser() {
        return packetEvent.getUser();
    }

    public String getUserName() {
        return packetEvent.getUser().getName();
    }

    public UUID getUserUUID() {
        return packetEvent.getUser().getUUID();
    }

    public void cancel() {
        packetEvent.setCancelled(true);
    }

}
