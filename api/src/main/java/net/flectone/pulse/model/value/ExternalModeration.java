package net.flectone.pulse.model.value;

/**
 * A punishment owned by another plugin, normalized into the shape FlectonePulse renders.
 *
 * @param playerName the punished player
 * @param moderatorName who issued the punishment
 * @param reason the stated reason, may be null
 * @param moderationId the id used by the source plugin
 * @param date when the punishment was issued, in milliseconds
 * @param time when it expires, in milliseconds
 * @param permanent whether it never expires
 * @author TheFaser
 */
public record ExternalModeration(
        String playerName,
        String moderatorName,
        String reason,
        long moderationId,
        long date,
        long time,
        boolean permanent
) {
}
