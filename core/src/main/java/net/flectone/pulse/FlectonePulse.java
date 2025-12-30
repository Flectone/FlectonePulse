package net.flectone.pulse;

import net.flectone.pulse.exception.ReloadException;

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
 * @see FlectonePulseAPI#getInstance()
 * @since 0.1.0
 */
public interface FlectonePulse {

    /**
     * Retrieves an instance of the specified class through dependency injection.
     * Uses Google Guice as the underlying dependency injection framework.
     *
     * <p><b>Note:</b> Most FlectonePulse classes (except models) are marked with {@code @Singleton}.
     *
     * @param <T> the type of instance to retrieve
     * @param type the class of the instance to retrieve
     * @return an instance of the requested type
     * @throws IllegalStateException if the injector is not ready
     *
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
     *
     * @see #get(Class)
     */
    boolean isReady();

    /**
     * Called when the FlectonePulse is enabled.
     * <p>
     * This method initializes the dependency injector and prepares the FlectonePulse
     * for operation. It should only be called by the FlectonePulse itself.
     *
     */
    void onEnable();

    /**
     * Called when the FlectonePulse is disabled.
     * <p>
     * This method cleans up resources and shuts down FlectonePulse modules.
     * It should only be called by the FlectonePulse itself.
     *
     */
    void onDisable();

    /**
     * Reloads the FlectonePulse configuration and modules.
     * <p>
     * This method reinitializes the plugin with updated configuration files
     * and should be called when configuration changes are made at runtime.
     *
     * @throws ReloadException if an error occurs during reload
     *
     * @see ReloadException
     */
    void reload() throws ReloadException;

}