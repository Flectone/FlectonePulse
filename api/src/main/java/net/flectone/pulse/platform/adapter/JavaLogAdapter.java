package net.flectone.pulse.platform.adapter;

import com.alessiodp.libby.logging.LogLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * An adapter that writes records to the JDK logger
 *
 * @author TheFaser
 */
public class JavaLogAdapter implements LogAdapter {

    private final Logger logger;

    public JavaLogAdapter(@NotNull Logger logger) {
        this.logger = logger;
    }

    /**
     * Logs a message at the given level.
     *
     * @param level the level
     * @param message the message
     * @param formatted whether the message was already formatted by the caller
     */
    @Override
    public void log(@NotNull LogLevel level, @Nullable String message, boolean formatted) {
        log(level, message, null, formatted);
    }

    /**
     * Logs a failure at the given level.
     *
     * @param level the level
     * @param message the message
     * @param throwable the failure
     * @param formatted whether the message was already formatted by the caller
     */
    @Override
    public void log(@NotNull LogLevel level, @Nullable String message, @Nullable Throwable throwable, boolean formatted) {
        LogRecord logRecord = new LogRecord(switch (level) {
            case DEBUG -> Level.FINE;
            case INFO -> Level.INFO;
            case WARN -> Level.WARNING;
            case ERROR -> Level.SEVERE;
        }, message);

        logRecord.setLoggerName(formatted ? "" : "FlectonePulse");

        if (throwable != null) {
            logRecord.setThrown(throwable);
        }

        logger.log(logRecord);
    }

}