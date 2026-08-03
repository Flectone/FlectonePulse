package net.flectone.pulse;

import net.flectone.pulse.util.constant.HookType;

/**
 * Bootstrap interface for platform-specific loader implementations.
 * Defines core lifecycle methods and provides access to dependency injection
 * before the full {@link FlectonePulse} API is available.
 *
 * @author TheFaser
 * @see FlectonePulse
 * @since 1.13.0
 */
public interface LoaderBoostrap {

    /**
     * Called when the plugin/mod is loaded
     */
    void onLoad();

    /**
     * Called when the plugin/mod is enabled
     */
    default void onEnable() {}

    /**
     * Called when the plugin/mod is disabled
     */
    default void onDisable() {}

    /**
     * Retrieves an instance of the specified class through dependency injection
     *
     * @param <T> the type of instance to retrieve
     * @param type the class of the instance to retrieve
     * @return an instance of the requested type
     */
    <T> T get(Class<T> type);

    /**
     * Registers or triggers a platform-specific hook
     * <p>
     * Hooks are used to integrate with external systems, modify plugin/mod
     * behavior, or notify internal components about certain events.
     * The meaning of the arguments depends on the {@link HookType}.
     *
     * @param type the type of hook to invoke
     * @param args variable arguments required by the hook
     */
    void hook(HookType type, Object... args);

}