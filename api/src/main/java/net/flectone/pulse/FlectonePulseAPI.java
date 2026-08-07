package net.flectone.pulse;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.exception.ReloadException;

/**
 * Lifecycle entry point that enables, disables and reloads the plugin, and holds the static handle
 * to the running instance
 * @author TheFaser
 */
public abstract class FlectonePulseAPI {

    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected static FlectonePulse instance;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected static boolean disabling;

    /**
     * Brings the plugin up, opening the database, registering listeners and enabling the modules.
     */
    abstract void onEnable();

    /**
     * Takes the plugin down, saving player state and closing everything it opened.
     */
    abstract void onDisable();

    /**
     * Re-reads the config and rebuilds everything that depends on it, without a restart.
     *
     * @throws ReloadException if any step of the reload fails
     */
    abstract void reload() throws ReloadException;

}
