package net.flectone.pulse.module.command.rockpaperscissors.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public class RockPaperScissors {

    private final UUID sender;
    private final UUID receiver;
    private final UUID id;

    @Setter
    private String senderMove;

    public RockPaperScissors(UUID sender, UUID receiver) {
        this(UUID.randomUUID(), sender, receiver);
    }

    public RockPaperScissors(UUID id, UUID sender, UUID receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.id = id;
    }
}
