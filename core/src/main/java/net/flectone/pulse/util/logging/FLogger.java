package net.flectone.pulse.util.logging;

import com.google.inject.Singleton;
import io.github.retrooper.packetevents.adventure.serializer.gson.GsonComponentSerializer;
import net.flectone.pulse.BuildConfig;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@Singleton
public class FLogger extends Logger {

    private final String pluginName = "\033[0;34m(FlectonePulse) \033[0m";

    private final List<String> pluginInfo = List.of(
            " \033[0;34m ___       ___  __  ___  __        ___ \033[0m",
            " \033[0;34m|__  |    |__  /  `  |  /  \\ |\\ | |__  \033[0m",
            " \033[0;34m|    |___ |___ \\__,  |  \\__/ | \\| |___ \033[0m",
            " \033[0;34m __             __   ___ \033[0;96m                       \033[0m",
            " \033[0;34m|__) |  | |    /__` |__  \033[0;96m                   \033[0m",
            " \033[0;34m|    \\__/ |___ .__/ |___\033[0;96m   /\\         \033[0m",
            " \033[0;96m                          /  \\ v<version>               \033[0m",
            " \033[0;96m__/\\___  ____/\\_____  ___/    \\______ \033[0m",
            " \033[0;96m       \\/           \\/  \033[0m"
    );

    private final Consumer<LogRecord> logConsumer;

    private LogFilter logFilter;

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

    public void enableFilter() {
        this.logFilter = new LogFilter();
    }

    public void reload(List<String> messages) {
        if (logFilter == null) return;

        logFilter.getMessages().clear();
        logFilter.getMessages().addAll(messages);
    }

    @Override
    public void log(LogRecord logRecord) {
        String colorLog = switch (logRecord.getLevel().intValue()) {
            // warn
            case 900 -> "\033[0;93m";
            // info
            case 800 -> "\033[0;96m";

            default -> "";
        };

        logRecord.setLoggerName("");
        logRecord.setMessage(pluginName + colorLog + logRecord.getMessage() + "\033[0m");

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

    public void logPluginInfo() {
        pluginInfo.forEach(string -> {
            string = string.replace("<version>", BuildConfig.PROJECT_VERSION);
            info(string);
        });
    }

    @Override
    public void warning(String exception) {
        LogRecord logRecord = new LogRecord(Level.WARNING, exception);

        log(logRecord);
    }

    public void warning(Throwable throwable) {
        LogRecord logRecord = new LogRecord(Level.WARNING, "An error occurred, report it to https://github.com/Flectone/FlectonePulse/issues");
        logRecord.setThrown(throwable);

        log(logRecord);
    }

    public void warningTree(Component component) {
        String json = GsonComponentSerializer.gson().serialize(component);
        warning(json);
    }
}
