package net.flectone.pulse.platform.formatter;

import net.flectone.pulse.checker.MuteChecker;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.ExternalModeration;
import net.flectone.pulse.model.value.Moderation;

import java.util.Optional;

/**
 * Fills the punishment placeholders for id, reason, dates and remaining time into the
 * messages shown to a punished player.
 * @author TheFaser
 */
public interface ModerationMessageFormatter {

    /**
     * Builds the message telling a player why they may not speak.
     *
     * @param fPlayer the silenced player
     * @param status what is stopping them
     * @return the message, or empty if nothing is
     */
    Optional<MessageContext> createMuteContext(FPlayer fPlayer, MuteChecker.Status status);

    /**
     * Fills in the placeholders from a punishment issued by this plugin.
     *
     * @param message the text with placeholders
     * @param fReceiver the player the text is for, which decides the date and time wording
     * @param moderation the punishment
     * @return the filled-in text
     */
    String replacePlaceholders(String message, FPlayer fReceiver, Moderation moderation);

    /**
     * Fills in the placeholders from a punishment owned by another plugin.
     *
     * @param message the text with placeholders
     * @param fReceiver the player the text is for
     * @param moderation the punishment
     * @return the filled-in text
     */
    String replacePlaceholders(String message, FPlayer fReceiver, ExternalModeration moderation);

    /**
     * Fills in the placeholders from loose values, for punishments that were never stored.
     *
     * @param message the text with placeholders
     * @param fReceiver the player the text is for
     * @param moderationId the punishment id
     * @param date when it was issued, in milliseconds
     * @param time when it expires, in milliseconds
     * @param reason the stated reason
     * @param permanent whether it never expires
     * @return the filled-in text
     */
    String replacePlaceholders(String message, FPlayer fReceiver, long moderationId, long date, long time, String reason, boolean permanent);

    /**
     * Fills in the placeholders from values that are already formatted for display.
     *
     * @param message the text with placeholders
     * @param moderationId the punishment id
     * @param reason the stated reason
     * @param date the formatted issue date
     * @param time the formatted expiry
     * @param timeLeft the formatted remaining time
     * @return the filled-in text
     */
    String replacePlaceholders(String message, String moderationId, String reason, String date, String time, String timeLeft);

}