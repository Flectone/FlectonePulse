package net.flectone.pulse.model.event.message;

import lombok.With;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.util.constant.ModuleName;

@With
public record MessagePrepareEvent(
        boolean cancelled,
        Type type,
        ModuleName moduleName,
        String rawFormat,
        EventMetadata<?> eventMetadata
) implements Event {


    public MessagePrepareEvent(ModuleName moduleName, String rawFormat, EventMetadata<?> eventMetadata) {
        this(false, Type.PROXY_INTEGRATION, moduleName, rawFormat, eventMetadata);
    }

    public MessagePrepareEvent(Type type, ModuleName moduleName, String rawFormat, EventMetadata<?> eventMetadata) {
        this(false, type, moduleName, rawFormat, eventMetadata);
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