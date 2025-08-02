package net.flectone.pulse.model.event.message;

import lombok.Getter;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.kyori.adventure.text.Component;

import java.util.UUID;

@Getter
public class SenderToReceiverMessageEvent extends Event {

    private final UUID uuid;
    private final FEntity sender;
    private final FPlayer receiver;
    private final Component message;
    private final Component submessage;
    private final Destination destination;

    public SenderToReceiverMessageEvent(UUID uuid,
                                        FEntity sender,
                                        FPlayer receiver,
                                        Component message,
                                        Component submessage,
                                        Destination destination) {
        this.uuid = uuid;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.submessage = submessage;
        this.destination = destination;
    }

}
