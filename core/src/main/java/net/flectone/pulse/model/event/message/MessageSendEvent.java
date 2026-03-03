package net.flectone.pulse.model.event.message;

import lombok.With;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.util.constant.ModuleName;
import net.kyori.adventure.text.Component;

@With
public record MessageSendEvent(
        boolean cancelled,
        ModuleName moduleName,
        FEntity sender,
        FPlayer receiver,
        Component message,
        Component submessage,
        EventMetadata<?> eventMetadata
) implements Event {

    public MessageSendEvent(ModuleName moduleName,
                            FPlayer receiver,
                            Component message,
                            Component submessage,
                            EventMetadata<?> eventMetadata) {
        this(false, moduleName, eventMetadata.sender(), receiver, message, submessage, eventMetadata);
    }

    public MessageSendEvent(ModuleName moduleName,
                            FPlayer sender,
                            Component message) {
        this(moduleName, sender, message, Component.empty(), EventMetadata.builder().sender(sender).format("").build());
    }

}