package net.flectone.pulse.platform.adapter;

import com.alessiodp.libby.logging.LogLevel;
import com.alessiodp.libby.logging.adapters.LogAdapter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import static java.util.Objects.requireNonNull;

public class NeoforgeLogAdapter implements LogAdapter {

    @NonNull
    private final Logger logger;

    public NeoforgeLogAdapter(@NonNull Logger logger) {
        this.logger = requireNonNull(logger, "logger");
    }

    @Override
    public void log(@NonNull LogLevel level, @Nullable String message) {
        switch (requireNonNull(level, "level")) {
            case DEBUG:
                logger.debug(message);
                break;
            case INFO:
                logger.info(message);
                break;
            case WARN:
                logger.warn(message);
                break;
            case ERROR:
                logger.error(message);
                break;
        }
    }

    @Override
    public void log(@NonNull LogLevel level, @Nullable String message, @Nullable Throwable throwable) {
        switch (requireNonNull(level, "level")) {
            case DEBUG:
                logger.debug(message, throwable);
                break;
            case INFO:
                logger.info(message, throwable);
                break;
            case WARN:
                logger.warn(message, throwable);
                break;
            case ERROR:
                logger.error(message, throwable);
                break;
        }
    }
}
