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
        switch (logLevel) {
            case INFO -> logger.at(Level.INFO).log(string);
            case DEBUG -> logger.at(Level.FINE).log(string);
            case WARN -> logger.at(Level.WARNING).log(string);
            case ERROR -> logger.at(Level.SEVERE).log(string);
        }
    }

    @Override
    public void log(@NonNull LogLevel logLevel, @Nullable String string, @Nullable Throwable throwable, boolean formatted) {
        switch (logLevel) {
            case INFO -> logger.at(Level.INFO).log(string, throwable);
            case DEBUG -> logger.at(Level.FINE).log(string, throwable);
            case WARN -> logger.at(Level.WARNING).log(string, throwable);
            case ERROR -> logger.at(Level.SEVERE).log(string, throwable);
        }
    }

}
