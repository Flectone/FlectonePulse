package net.flectone.pulse.model.event.message;

import lombok.With;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.util.UUID;

@With
public record ProxyMessageEvent(
        boolean cancelled,
        boolean processed,
        boolean sentByThisServer,
        String server,
        ModuleName name,
        FEntity sender,
        UUID uuid,
        byte[] payload
) implements Event {

    public ProxyMessageEvent(boolean sentByThisServer, String server, ModuleName name, FEntity sender, UUID uuid, byte[] payload) {
        this(false, false, sentByThisServer, server, name, sender, uuid, payload);
    }

    public ProxyPayload openPayload() {
        return new ProxyPayload(payload);
    }

}
