package net.flectone.pulse.platform.formatter;

import net.flectone.pulse.model.entity.FPlayer;

/**
 * Renders durations and dates in the reader's own language.
 * @author TheFaser
 */
public interface TimeFormatter {

    // 1s = 20ticks -> 20ticks * 50 = 1000ms -> 1s = 1000ms
    /**
     * Milliseconds per tick, used to convert the tick counts the config uses.
     */
    long MULTIPLIER = 50L;

    /**
     * Renders a duration using the player's language.
     *
     * @param fPlayer the reader
     * @param time the duration in milliseconds
     * @return the rendered duration
     */
    String format(FPlayer fPlayer, long time);

    /**
     * Renders a duration into a pattern that decides which units are shown.
     *
     * @param fPlayer the reader
     * @param time the duration in milliseconds
     * @param message the pattern from the localization file
     * @return the rendered duration
     */
    String format(FPlayer fPlayer, long time, String message);

    /**
     * Renders a point in time using the format configured for the server.
     *
     * @param date the timestamp in milliseconds
     * @return the rendered date
     */
    String formatDate(long date);

}
