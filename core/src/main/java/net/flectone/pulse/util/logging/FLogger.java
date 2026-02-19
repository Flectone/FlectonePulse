package net.flectone.pulse.util.logging;

import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.util.file.FileFacade;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;

@Singleton
public record FLogger(
        Consumer<LogRecord> logConsumer,
        Supplier<FileFacade> fileFacadeSupplier
) {

    private static final boolean ANSI_SUPPORTED = isAnsiSupported();

    public Config.Logger config() {
        return fileFacadeSupplier.get() == null ? null : fileFacadeSupplier.get().config().logger();
    }

    public void log(LogRecord logRecord) {
        Config.Logger config = config();
        if (config == null) {
            logRecord.setLoggerName("FlectonePulse");
            logConsumer.accept(logRecord);
            return;
        }

        String color = "";
        if (ANSI_SUPPORTED) {
            color = switch (logRecord.getLevel().intValue()) {
                case 900 -> config.warn();
                case 800 -> config.info();
                default -> "";
            };
        }

        String prefix = config.prefix();

        if (ANSI_SUPPORTED && !color.isEmpty()) {
            logRecord.setMessage(prefix + color + logRecord.getMessage() + "\033[0m");
        } else {
            logRecord.setMessage(prefix + logRecord.getMessage());
        }

        logRecord.setLoggerName("");
        logConsumer.accept(logRecord);
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

    public void info(String string) {
        log(new LogRecord(Level.INFO, string));
    }

    public void warning(String string) {
        log(new LogRecord(Level.WARNING, string));
    }

    public void warning(Throwable throwable) {
        warning("An error occurred, report it to https://github.com/Flectone/FlectonePulse/issues", throwable);
    }

    public void warning(String exception, Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);

        log(new LogRecord(Level.WARNING, exception + "\n" + stringWriter));
    }

    // Idea taken from net.kyori.ansi.ColorLevel
    private static boolean isAnsiSupported() {
        if (System.console() == null) return false;

        String colorterm = System.getenv("COLORTERM");
        if (colorterm != null && (colorterm.contains("truecolor") || colorterm.contains("24bit"))) return true;

        String term = System.getenv("TERM");
        if (term != null && (term.contains("truecolor") || term.contains("direct") || term.contains("256color"))) return true;
        if (System.getenv("WT_SESSION") != null) return true;

        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        return !os.contains("win");
    }

}
