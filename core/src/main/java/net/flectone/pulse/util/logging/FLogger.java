package net.flectone.pulse.util.logging;

import com.google.inject.Singleton;
import io.github.retrooper.packetevents.adventure.serializer.gson.GsonComponentSerializer;
import lombok.Setter;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@Singleton
public class FLogger extends Logger {

    private final Consumer<LogRecord> logConsumer;
    private final LogFilter logFilter = new LogFilter();

    @Setter private FileFacade fileFacade;

    public FLogger(Consumer<LogRecord> logConsumer) {
        super("", null);

        this.logConsumer = logConsumer;
    }

    public FLogger(Logger logger) {
        super("", null);

        logger.setLevel(Level.OFF);
        setParent(logger);
        setLevel(Level.ALL);

        logConsumer = super::log;
    }

    public Config.Logger config() {
        return fileFacade == null ? null : fileFacade.config().logger();
    }

    public void setupFilter() {
        logFilter.setFilters(config() == null ? Collections.emptyList() : config().filter());
    }

    @Override
    public void log(LogRecord logRecord) {
        if (config() == null) {
            logRecord.setLoggerName("FlectonePulse");
            logConsumer.accept(logRecord);
            return;
        }

        String prefix = config().prefix();

        String color = switch (logRecord.getLevel().intValue()) {
            case 900 -> config().warn();
            case 800 -> config().info();
            default -> "";
        };

        logRecord.setLoggerName("");
        logRecord.setMessage(prefix + color + logRecord.getMessage() + "\033[0m");

        logConsumer.accept(logRecord);
    }

    @Override
    public void info(String msg) {
        LogRecord logRecord = new LogRecord(Level.INFO, msg);

        log(logRecord);
    }

    public void logEnabling() {
        info("Enabling...");
    }

    public void logEnabled() {
        info("FlectonePulse v" + BuildConfig.PROJECT_VERSION + " enabled");
    }

    public void logDisabling() {
        info("Disabling...");
    }

    public void logDisabled() {
        info("FlectonePulse v" + BuildConfig.PROJECT_VERSION + " disabled");
    }

    public void logReloading() {
        info("Reloading...");
    }

    public void logReloaded() {
        info("FlectonePulse v" + BuildConfig.PROJECT_VERSION + " reloaded");
    }

    public void logDescription() {
        Config.Logger config = config();
        if (config == null) return;

        config.description().forEach(string -> {
            string = string.replace("<version>", BuildConfig.PROJECT_VERSION);
            info(string);
        });
    }

    @Override
    public void warning(String exception) {
        LogRecord logRecord = new LogRecord(Level.WARNING, exception);

        log(logRecord);
    }

    public void warning(String exception, Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);

        LogRecord logRecord = new LogRecord(Level.WARNING, exception + "\n" + stringWriter);
        log(logRecord);
    }

    public void warning(Throwable throwable) {
        warning("An error occurred, report it to https://github.com/Flectone/FlectonePulse/issues", throwable);
    }

    public void warningTree(Component component) {
        String json = GsonComponentSerializer.gson().serialize(component);
        warning(json);
    }
}
