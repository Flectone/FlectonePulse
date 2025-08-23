package net.flectone.pulse.model.event.message;

import lombok.Getter;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.metadata.MessageMetadata;
import net.flectone.pulse.model.util.Destination;
import net.kyori.adventure.text.Component;

import java.util.UUID;

/**
 * This event is fired everytime a message between a {@link FEntity} and a {@link FPlayer} is sent.
 * However, the sender and receiver can be the same, likely indicating the message is a <code>personalMessage</code>.
 *
 * @see MessageMetadata
 * @see FPlayer
 */
@Getter
public class SenderToReceiverMessageEvent extends Event {

    private final UUID messageUUID;
    private final FEntity sender;
    private final FPlayer receiver;
    private final Component message;
    private final Component submessage;
    private final Destination destination;
    private final MessageMetadata metadata;

    public SenderToReceiverMessageEvent(UUID messageUUID,
                                        FEntity sender,
                                        FPlayer receiver,
                                        Component message,
                                        Component submessage,
                                        Destination destination,
                                        MessageMetadata metadata) {
        this.messageUUID = messageUUID;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.submessage = submessage;
        this.destination = destination;
        this.metadata = metadata;
    }

    public SenderToReceiverMessageEvent(FEntity sender,
                                        FPlayer receiver,
                                        Component message,
                                        Component submessage,
                                        Destination destination,
                                        MessageMetadata metadata) {
        this(UUID.randomUUID(), sender, receiver, message, submessage, destination, metadata);
    }

    public SenderToReceiverMessageEvent(FPlayer sender,
                                        Component message,
                                        Component submessage,
                                        Destination destination,
                                        MessageMetadata metadata) {
        this(UUID.randomUUID(), sender, sender, message, submessage, destination, metadata);
    }

    public SenderToReceiverMessageEvent(FPlayer sender,
                                        Component message,
                                        Destination destination,
                                        MessageMetadata metadata) {
        this(UUID.randomUUID(), sender, sender, message, Component.empty(), destination, metadata);
    }

    public SenderToReceiverMessageEvent(FPlayer sender,
                                        Component message,
                                        MessageMetadata metadata) {
        this(UUID.randomUUID(), sender, sender, message, Component.empty(), new Destination(), metadata);
    }
}
