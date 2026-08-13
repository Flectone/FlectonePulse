package net.flectone.pulse.platform.adapter;

import com.alessiodp.libby.logging.LogLevel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class FabricLogAdapter implements LogAdapter {

    private final Logger logger;

    public FabricLogAdapter(Logger logger) {
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
