package net.flectone.pulse;

import net.flectone.pulse.exception.InitException;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.util.logging.FLogger;
import org.jspecify.annotations.NonNull;

/**
 * Main interface for accessing FlectonePulse API functionality.
 * Provides dependency injection capabilities and plugin lifecycle management.
 *
 * <p><b>Example usage:</b>
 * <pre>{@code
 * // Get the FlectonePulse instance
 * FlectonePulse flectonePulse = FlectonePulseAPI.getInstance();
 *
 * // Check if the injector is ready
 * if (flectonePulse.isReady()) {
 *     // Get a dependency
 *     FLogger logger = flectonePulse.get(FLogger.class);
 *     logger.info("Hello world");
 * }
 * }</pre>
 *
 * @author TheFaser
 * @since 0.1.0
 */
public interface FlectonePulse extends LoaderBootstrap {

    /**
     * Reloads the FlectonePulse configuration and modules.
     * <p>
     * This method reinitializes the plugin with updated configuration files
     * and should be called when configuration changes are made at runtime.
     *
     * @throws ReloadException if an error occurs during reload
     * @see ReloadException
     */
    void reload() throws ReloadException;

    /**
     * Returns the platform-specific plugin loader instance.
     * <p>
     * This object represents the native plugin or mod container for the
     * current server implementation (e.g., {@code JavaPlugin} on Bukkit,
     * {@code Plugin} on BungeeCord, {@code Object} on Velocity).
     * It can be used to access platform APIs that are not abstracted by
     * the FlectonePulse interface.
     *
     * @return the native loader instance
     */
    @NonNull Object getLoader();

    /**
     * Retrieves an instance of the specified class through dependency injection.
     * Uses Google Guice as the underlying dependency injection framework.
     *
     * @param <T> the type of instance to retrieve
     * @param type the class of the instance to retrieve
     * @return an instance of the requested type
     * @throws IllegalStateException if the injector is not ready
     * @see #isReady()
     */
    <T> T get(Class<T> type);

    /**
     * Checks if the dependency injector is ready to provide instances.
     *
     * <p><b>Important:</b> Always call this method before {@link #get(Class)}
     * to ensure the injector has been properly initialized.
     *
     * @return {@code true} if the injector is ready, {@code false} otherwise
     * @see #get(Class)
     */
    boolean isReady();

    /**
     * Throws an InitException with the message from the provided exception.
     *
     * <p>In production mode (when -Dflectonepulse.debug is not set to true), the error
     * message is truncated to the first 25 lines to prevent excessive log output.
     * In debug mode, the full exception message is preserved.
     *
     * @param e the original exception whose message will be included in the InitException
     * @throws InitException always thrown with the processed error message
     */
    default void throwInitException(Exception e) throws InitException {
        String errorMessage = e.getMessage();

        if (!FLogger.DEBUG_ENABLED) {
            String[] lines = e.getMessage().split("\n");
            StringBuilder stringBuilder = new StringBuilder();

            for (int i = 0; i < Math.min(25, lines.length); i++) {
                stringBuilder.append(lines[i]).append("\n");
            }

            errorMessage = stringBuilder.toString();
        }

        throw new InitException(errorMessage);
    }

}