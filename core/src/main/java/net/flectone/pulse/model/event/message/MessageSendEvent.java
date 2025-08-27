package net.flectone.pulse.model.event.message;

import lombok.Getter;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;

/**
 * This event is fired everytime a message between a {@link FEntity} and a {@link FPlayer} is sent.
 *
 * @see EventMetadata
 * @see FPlayer
 */
@Getter
public class MessageSendEvent extends Event {

    private final MessageType messageType;
    private final FEntity sender;
    private final FPlayer receiver;
    private final Component message;
    private final Component submessage;
    private final EventMetadata<?> eventMetadata;

    public MessageSendEvent(MessageType messageType,
                            FEntity sender,
                            FPlayer receiver,
                            Component message,
                            Component submessage,
                            EventMetadata<?> eventMetadata) {
        this.messageType = messageType;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.submessage = submessage;
        this.eventMetadata = eventMetadata;
    }

    public MessageSendEvent(MessageType messageType,
                            FPlayer sender,
                            Component message,
                            Component submessage,
                            EventMetadata<?> eventMetadata) {
        this(messageType, sender, sender, message, submessage, eventMetadata);
    }

    public MessageSendEvent(MessageType messageType,
                            FPlayer sender,
                            Component message,
                            EventMetadata<?> eventMetadata) {
        this(messageType, sender, message, Component.empty(), eventMetadata);
    }

    public MessageSendEvent(MessageType messageType,
                            FPlayer sender,
                            Component message) {
        this(messageType, sender, message, EventMetadata.empty());
    }

}
