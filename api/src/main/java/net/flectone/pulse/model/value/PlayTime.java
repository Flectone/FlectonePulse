package net.flectone.pulse.model.value;

/**
 * Aggregated session statistics for a single player.
 *
 * @param id the database id
 * @param playerId the player this row belongs to
 * @param first when the player was first seen, in milliseconds
 * @param last when the player was last seen, in milliseconds
 * @param total the total time played, in milliseconds
 * @param sessions how many times the player has joined
 * @author TheFaser
 */
public record PlayTime(
        int id,
        int playerId,
        long first,
        long last,
        long total,
        int sessions
) {
}
