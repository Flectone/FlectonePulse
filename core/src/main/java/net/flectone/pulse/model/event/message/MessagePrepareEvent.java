package net.flectone.pulse.model.event.message;

import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.util.constant.ModuleName;

import java.util.Set;

@With
public record MessagePrepareEvent(
        boolean cancelled,
        Type type,
        ModuleName moduleName,
        EventMetadata eventMetadata,
        MessageContext messageContext,
        Set<FPlayer> receivers
) implements Event {


    public MessagePrepareEvent(ModuleName moduleName, EventMetadata eventMetadata, MessageContext messageContext) {
        this(false, Type.PROXY_INTEGRATION, moduleName, eventMetadata, messageContext, Set.of());
    }

    public MessagePrepareEvent(Type type, ModuleName moduleName, EventMetadata eventMetadata) {
        this(false, type, moduleName, eventMetadata, eventMetadata.resolveMessageContext(FPlayer.UNKNOWN), Set.of());
    }

    public boolean isForProxy() {
        return this.type == Type.PROXY_INTEGRATION || this.type == Type.PROXY;
    }

    public boolean isForIntegration() {
        return this.type == Type.PROXY_INTEGRATION || this.type == Type.INTEGRATION;
    }

    public enum Type {
        PROXY,
        INTEGRATION,
        PROXY_INTEGRATION,
    }

}