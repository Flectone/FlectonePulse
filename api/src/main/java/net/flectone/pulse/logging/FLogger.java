package net.flectone.pulse.logging;

import com.alessiodp.libby.logging.LogLevel;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.platform.adapter.LogAdapter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * The plugin's logger. It writes through the platform's own logger and, where the console
 * supports it, renders the colors a message carries.
 *
 * @param logAdapter hands a finished record to the platform logger
 * @param fileFacadeSupplier supplies the config, which is not loaded yet when the logger is created
 * @author TheFaser
 */
public record FLogger(
        LogAdapter logAdapter,
        Supplier<FileFacade> fileFacadeSupplier
) {

    /**
     * Whether debug output is switched on by the system property.
     */
    public static final boolean DEBUG_ENABLED = Boolean.parseBoolean(System.getProperty("flectonepulse.debug", "false"));
    /**
     * The line asking the user to report an unexpected failure.
     */
    public static final String ERROR_MESSAGE_REPORT = "An error occurred, report it to https://github.com/Flectone/FlectonePulse/issues \n";

    private static final boolean ANSI_SUPPORTED = isAnsiSupported();
    private static final String RESET_COLOR = "\033[0m";

    // Idea taken from net.kyori.ansi.ColorLevel
    private static boolean isAnsiSupported() {
        if (System.console() == null) return false;

        String colorterm = System.getenv("COLORTERM");
        if (colorterm != null && (colorterm.contains("truecolor") || colorterm.contains("24bit"))) return true;

        String term = System.getenv("TERM");
        if (term != null && (term.contains("truecolor") || term.contains("direct") || term.contains("256color")))
            return true;
        if (System.getenv("WT_SESSION") != null) return true;

        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        return !os.contains("win");
    }

    /**
     * The logger settings, or null while the config is still loading.
     *
     * @return the settings, or null
     */
    public Config.@Nullable Logger config() {
        return fileFacadeSupplier.get() == null ? null : fileFacadeSupplier.get().config().logger();
    }

    /**
     * Logs that the plugin is starting up.
     */
    public void logEnabling() {
        info("Enabling...");
    }

    /**
     * Logs that the plugin has started.
     */
    public void logEnabled() {
        info("FlectonePulse v%s enabled", BuildConfig.PROJECT_VERSION);
    }

    /**
     * Logs that the plugin is shutting down.
     */
    public void logDisabling() {
        info("Disabling...");
    }

    /**
     * Logs that the plugin has shut down.
     */
    public void logDisabled() {
        info("FlectonePulse v%s disabled", BuildConfig.PROJECT_VERSION);
    }

    /**
     * Logs that a reload has begun.
     */
    public void logReloading() {
        info("Reloading...");
    }

    /**
     * Logs that a reload has finished.
     */
    public void logReloaded() {
        info("FlectonePulse v%s reloaded", BuildConfig.PROJECT_VERSION);
    }

    /**
     * Logs the plugin banner with its version and links.
     */
    public void logDescription() {
        Config.Logger config = config();
        if (config == null) return;

        config.description().forEach(string -> {
            string = string.replace("<version>", BuildConfig.PROJECT_VERSION);
            info(string);
        });
    }

    /**
     * Logs a message.
     *
     * @param string the message
     */
    public void info(@Nullable String string) {
        log(LogLevel.INFO, string);
    }

    /**
     * Logs a formatted message.
     *
     * @param format the format string
     * @param args the arguments
     */
    public void info(@NonNull String format, Object... args) {
        info(String.format(format, args));
    }

    /**
     * Logs a warning, rendering the value as text.
     *
     * @param object the value
     */
    public void warning(@Nullable Object object) {
        warning(String.valueOf(object));
    }

    /**
     * Logs a warning.
     *
     * @param string the message
     */
    public void warning(@Nullable String string) {
        log(LogLevel.WARN, string);
    }

    /**
     * Logs a formatted warning.
     *
     * @param format the format string
     * @param args the arguments
     */
    public void warning(@NonNull String format, Object... args) {
        warning(String.format(format, args));
    }

    /**
     * Logs a failure with its stack trace under a formatted message.
     *
     * @param format the format string
     * @param throwable the failure
     * @param args the arguments
     */
    public void warning(@NonNull String format, @Nullable Throwable throwable, Object... args) {
        warning(String.format(format, args), throwable);
    }

    /**
     * Logs a failure with its stack trace and a request to report it.
     *
     * @param throwable the failure
     */
    public void warning(@Nullable Throwable throwable) {
        warning(ERROR_MESSAGE_REPORT, throwable);
    }

    /**
     * Logs a failure with its stack trace under a message of your own.
     *
     * @param string the message
     * @param throwable the failure
     */
    public void warning(@Nullable String string, @Nullable Throwable throwable) {
        log(LogLevel.WARN, string, throwable);
    }

    /**
     * Logs a message at the given level with formatting applied.
     *
     * @param logLevel the level
     * @param string the message
     */
    public void log(@NonNull LogLevel logLevel, @Nullable String string) {
        Config.Logger config = config();
        if (config == null) {
            logAdapter.log(logLevel, string, false);
        } else {
            logAdapter.log(logLevel, formatMessage(logLevel, config, string), true);
        }
    }

    /**
     * Logs a failure at the given level with formatting applied.
     *
     * @param logLevel the level
     * @param string the message
     * @param throwable the failure
     */
    public void log(@NonNull LogLevel logLevel, @Nullable String string, @Nullable Throwable throwable) {
        Config.Logger config = config();
        if (config == null) {
            logAdapter.log(logLevel, string, throwable, false);
        } else {
            logAdapter.log(logLevel, formatMessage(logLevel, config, string), throwable, true);
        }
    }

    private @NonNull String formatMessage(@NonNull LogLevel logLevel, Config.@NonNull Logger config, @Nullable String message) {
        if (ANSI_SUPPORTED) {
            String color = switch (logLevel) {
                case WARN -> config.warn();
                case INFO -> config.info();
                default -> "";
            };
            return config.primary() + config.prefix() + RESET_COLOR + color + message + RESET_COLOR;
        } else {
            return config.prefix() + message;
        }
    }

}