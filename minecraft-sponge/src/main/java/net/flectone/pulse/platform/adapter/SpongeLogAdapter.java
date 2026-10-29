package net.flectone.pulse.platform.adapter;

import com.alessiodp.libby.logging.LogLevel;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SpongeLogAdapter implements LogAdapter {

    private final Logger logger;

    public SpongeLogAdapter(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(@NonNull LogLevel logLevel, @Nullable String string, boolean formatted) {
        switch (logLevel) {
            case INFO -> logger.info(string);
            case DEBUG -> logger.debug(string);
            case WARN -> logger.warn(string);
            case ERROR -> logger.error(string);
        }
    }

    @Override
    public void log(@NonNull LogLevel logLevel, @Nullable String string, @Nullable Throwable throwable, boolean formatted) {
        switch (logLevel) {
            case INFO -> logger.info(string, throwable);
            case DEBUG -> logger.debug(string, throwable);
            case WARN -> logger.warn(string, throwable);
            case ERROR -> logger.error(string, throwable);
        }
    }

}
