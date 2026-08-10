package net.flectone.pulse.module.integration;

import net.flectone.pulse.logging.FLogger;

/**
 * One hook into another plugin. Implementations report their name and log when they are wired up or torn down.
 * @author TheFaser
 */
public interface FIntegration {

    /**
     * The name of the plugin this hooks into, as written in the log.
     *
     * @return the plugin name
     */
    String getIntegrationName();

    /**
     * The logger the hook messages go to.
     *
     * @return the logger
     */
    FLogger getFLogger();

    /**
     * Wires the integration up. Override to do the work, then call the logging default.
     */
    default void hook() {
        logHook();
    }

    /**
     * Tears the integration down. Override to do the work, then call the logging default.
     */
    default void unhook() {
        logUnhook();
    }

    /**
     * Logs that the integration is now active.
     */
    default void logHook() {
        getFLogger().info("[+] Loaded integration: %s", getIntegrationName());
    }

    /**
     * Logs that the integration failed to load.
     *
     * @param exception the exception that caused the failure
     */
    default void lohHookFailed(Exception exception) {
        getFLogger().warning("[-] Failed to load integration: %s", getIntegrationName(), exception);
    }

    /**
     * Logs that the integration is no longer active.
     */
    default void logUnhook() {
        getFLogger().info("[-] Unloaded integration: %s", getIntegrationName());
    }

}
