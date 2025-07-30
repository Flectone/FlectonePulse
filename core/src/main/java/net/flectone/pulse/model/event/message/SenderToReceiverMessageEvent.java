package net.flectone.pulse.model.event.message;

import lombok.Getter;
import net.flectone.pulse.model.Destination;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.kyori.adventure.text.Component;

@Getter
public class SenderToReceiverMessageEvent extends Event {

    private final FEntity sender;
    private final FPlayer receiver;
    private final Component message;
    private final Component submessage;
    private final Destination destination;

    public SenderToReceiverMessageEvent(FEntity sender, FPlayer receiver, Component message, Component submessage, Destination destination) {
        super(Type.SENDER_TO_RECEIVER_MESSAGE);

        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.submessage = submessage;
        this.destination = destination;
    }

}
