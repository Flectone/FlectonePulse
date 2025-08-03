package net.flectone.pulse.model.event.message;

import lombok.Getter;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.util.Destination;
import net.kyori.adventure.text.Component;

import java.util.UUID;

@Getter
public class SenderToReceiverMessageEvent extends Event {

    private final UUID messageUUID;
    private final FEntity sender;
    private final FPlayer receiver;
    private final Component message;
    private final Component submessage;
    private final Destination destination;

    public SenderToReceiverMessageEvent(UUID messageUUID,
                                        FEntity sender,
                                        FPlayer receiver,
                                        Component message,
                                        Component submessage,
                                        Destination destination) {
        this.messageUUID = messageUUID;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.submessage = submessage;
        this.destination = destination;
    }

    public SenderToReceiverMessageEvent(FEntity sender,
                                        FPlayer receiver,
                                        Component message,
                                        Component submessage,
                                        Destination destination) {
        this(UUID.randomUUID(), sender, receiver, message, submessage, destination);
    }

    public SenderToReceiverMessageEvent(FPlayer sender,
                                        Component message,
                                        Component submessage,
                                        Destination destination) {
        this(UUID.randomUUID(), sender, sender, message, submessage, destination);
    }

    public SenderToReceiverMessageEvent(FPlayer sender, Component message, Destination destination) {
        this(UUID.randomUUID(), sender, sender, message, Component.empty(), destination);
    }

    public SenderToReceiverMessageEvent(FPlayer sender, Component message) {
        this(UUID.randomUUID(), sender, sender, message, Component.empty(), new Destination());
    }
}
