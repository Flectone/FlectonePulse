package net.flectone.pulse.module.command.mail.model;

/**
 * A message left for a player who was offline.
 *
 * @param id the database id
 * @param date when it was sent, in milliseconds
 * @param sender the sending player id
 * @param receiver the addressed player id
 * @param message the text
 * @author TheFaser
 */
public record Mail(
        int id,
        long date,
        int sender,
        int receiver,
        String message
) {
}
