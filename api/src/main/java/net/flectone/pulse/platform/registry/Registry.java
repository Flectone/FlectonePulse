package net.flectone.pulse.platform.registry;

/**
 * Something that registers things with the platform and has to undo that on shutdown or reload.
 * @author TheFaser
 */
public interface Registry {

    /**
     * Registers whatever this registry owns.
     */
    default void onEnable() {
    }

    /**
     * Unregisters whatever this registry owns.
     */
    default void onDisable() {
    }

}
