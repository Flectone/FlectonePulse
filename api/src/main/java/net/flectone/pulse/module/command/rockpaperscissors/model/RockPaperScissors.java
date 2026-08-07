package net.flectone.pulse.module.command.rockpaperscissors.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * A round of rock-paper-scissors between two players.
 * @author TheFaser
 */
@Getter
public class RockPaperScissors {

    private final UUID sender;
    private final UUID receiver;
    private final UUID id;

    @Setter private String senderMove;

    /**
     * Starts a round with a fresh id.
     *
     * @param sender who sent the invitation
     * @param receiver who was invited
     */
    public RockPaperScissors(UUID sender, UUID receiver) {
        this(UUID.randomUUID(), sender, receiver);
    }

    /**
     * Recreates a round that already has an id, used when it arrives from another server.
     *
     * @param id the game id
     * @param sender who sent the invitation
     * @param receiver who was invited
     */
    public RockPaperScissors(UUID id, UUID sender, UUID receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.id = id;
    }
}
