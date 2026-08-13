package net.flectone.pulse.platform.adapter;

import com.alessiodp.libby.logging.LogLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An adapter that writes records to the platform logger
 *
 * @author TheFaser
 */
public interface LogAdapter extends com.alessiodp.libby.logging.adapters.LogAdapter {

    /**
     * Logs a message at the given level.
     *
     * @param level the level
     * @param message the message
     * @param formatted whether the message was already formatted by the caller
     */
    void log(@NotNull LogLevel level, @Nullable String message, boolean formatted);

    /**
     * Logs a message at the given level.
     *
     * @param level the level
     * @param message the message
     */
    default void log(@NotNull LogLevel level, @Nullable String message) {
        log(level, message, false);
    }

    /**
     * Logs a failure at the given level.
     *
     * @param level the level
     * @param message the message
     * @param throwable the failure
     * @param formatted whether the message was already formatted by the caller
     */
    void log(@NotNull LogLevel level, @Nullable String message, @Nullable Throwable throwable, boolean formatted);

    /**
     * Logs a failure at the given level.
     *
     * @param level the level
     * @param message the message
     * @param throwable the failure
     */
    default void log(@NotNull LogLevel level, @Nullable String message, @Nullable Throwable throwable) {
        log(level, message, throwable, false);
    }

}