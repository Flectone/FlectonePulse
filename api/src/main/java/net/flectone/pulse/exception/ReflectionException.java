package net.flectone.pulse.exception;

/**
 * Thrown when the plugin cannot reach a server internal it needs through reflection,
 * usually because the running version moved or renamed it.
 *
 * @author TheFaser
 */
public class ReflectionException extends RuntimeException {

    /**
     * Creates the exception around the failure that broke the lookup.
     *
     * @param message what the plugin was looking for
     * @param cause the underlying failure
     */
    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }

}
