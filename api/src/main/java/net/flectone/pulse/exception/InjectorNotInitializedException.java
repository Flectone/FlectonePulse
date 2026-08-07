package net.flectone.pulse.exception;

/**
 * Thrown when a dependency is requested before the platform has built its injector,
 * which means the plugin was used before it finished starting.
 *
 * @author TheFaser
 */
public class InjectorNotInitializedException extends RuntimeException {

    /**
     * Creates the exception.
     */
    public InjectorNotInitializedException() {
        super("Injector has not yet been initialized");
    }

}
