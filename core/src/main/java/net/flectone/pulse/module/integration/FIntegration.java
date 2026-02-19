package net.flectone.pulse.module.integration;

import net.flectone.pulse.util.logging.FLogger;

public interface FIntegration {

    String getIntegrationName();

    FLogger getFLogger();

    default void hook() {
        logHook();
    }

    default void unhook() {
        logUnhook();
    }

    default void logHook() {
        getFLogger().info("[+] Loaded integration: " + getIntegrationName());
    }

    default void logUnhook() {
        getFLogger().info("[-] Unloaded integration: " + getIntegrationName());
    }

}
