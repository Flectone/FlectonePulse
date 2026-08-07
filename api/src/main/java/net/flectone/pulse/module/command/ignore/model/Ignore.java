package net.flectone.pulse.module.command.ignore.model;

/**
 * One entry on a player's ignore list.
 *
 * @param id the database id
 * @param date when it was added, in milliseconds
 * @param target the ignored player id
 * @author TheFaser
 */
public record Ignore(
        int id,
        long date,
        int target
) {
}
