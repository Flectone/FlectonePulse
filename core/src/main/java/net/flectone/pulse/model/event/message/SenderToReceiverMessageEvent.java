package net.flectone.pulse.model.event.message;

import lombok.Getter;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;

import java.util.UUID;

/**
 * This event is fired everytime a message between a {@link FEntity} and a {@link FPlayer} is sent.
 *
 * @see EventMetadata
 * @see FPlayer
 */
@Getter
public class SenderToReceiverMessageEvent extends Event {

    private final UUID messageUUID;
    private final MessageType messageType;
    private final FEntity sender;
    private final FPlayer receiver;
    private final Component message;
    private final Component submessage;
    private final Destination destination;
    private final EventMetadata eventMetadata;

    public SenderToReceiverMessageEvent(UUID messageUUID,
                                        MessageType messageType,
                                        FEntity sender,
                                        FPlayer receiver,
                                        Component message,
                                        Component submessage,
                                        Destination destination,
                                        EventMetadata eventMetadata) {
        this.messageUUID = messageUUID;
        this.messageType = messageType;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.submessage = submessage;
        this.destination = destination;
        this.eventMetadata = eventMetadata;
    }

    public SenderToReceiverMessageEvent(MessageType messageType,
                                        FEntity sender,
                                        FPlayer receiver,
                                        Component message,
                                        Component submessage,
                                        Destination destination,
                                        EventMetadata eventMetadata) {
        this(UUID.randomUUID(), messageType, sender, receiver, message, submessage, destination, eventMetadata);
    }

    public SenderToReceiverMessageEvent(MessageType messageType,
                                        FPlayer sender,
                                        Component message,
                                        Component submessage,
                                        Destination destination,
                                        EventMetadata eventMetadata) {
        this(messageType, sender, sender, message, submessage, destination, eventMetadata);
    }

    public SenderToReceiverMessageEvent(MessageType messageType,
                                        FPlayer sender,
                                        Component message,
                                        Destination destination,
                                        EventMetadata eventMetadata) {
        this(messageType, sender, message, Component.empty(), destination, eventMetadata);
    }

    public SenderToReceiverMessageEvent(MessageType messageType,
                                        FPlayer sender,
                                        Component message,
                                        EventMetadata eventMetadata) {
        this(messageType, sender, message, new Destination(), eventMetadata);
    }

    public SenderToReceiverMessageEvent(MessageType messageType,
                                        FPlayer sender,
                                        Component message) {
        this(messageType, sender, message, EventMetadata.EMPTY);
    }

}
