package net.flectone.pulse.model.event.message;

import lombok.With;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.util.constant.ModuleName;

@With
public record MessagePrepareEvent(
        boolean cancelled,
        ModuleName moduleName,
        String rawFormat,
        EventMetadata<?> eventMetadata
) implements Event {

    public MessagePrepareEvent(ModuleName moduleName, String rawFormat, EventMetadata<?> eventMetadata) {
        this(false, moduleName, rawFormat, eventMetadata);
    }

}