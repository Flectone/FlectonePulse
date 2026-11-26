package net.flectone.pulse.platform.adapter;

import com.alessiodp.libby.logging.LogLevel;
import com.hypixel.hytale.logger.HytaleLogger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.logging.Level;

public class HytaleLogAdapter implements LogAdapter {

    private final HytaleLogger logger;

    public HytaleLogAdapter(HytaleLogger logger) {
        this.logger = logger;
    }

    @Override
    public void log(@NonNull LogLevel logLevel, @Nullable String string, boolean formatted) {
        at(logLevel).log(string);
    }

    @Override
    public void log(@NonNull LogLevel logLevel, @Nullable String string, @Nullable Throwable throwable, boolean formatted) {
        HytaleLogger.Api api = at(logLevel);

        if (throwable != null) {
            api = api.withCause(throwable);
        }

        api.log(string);
    }

    private HytaleLogger.Api at(@NonNull LogLevel logLevel) {
        return switch (logLevel) {
            case INFO -> logger.at(Level.INFO);
            case DEBUG -> logger.at(Level.FINE);
            case WARN -> logger.at(Level.WARNING);
            case ERROR -> logger.at(Level.SEVERE);
        };
    }

}
