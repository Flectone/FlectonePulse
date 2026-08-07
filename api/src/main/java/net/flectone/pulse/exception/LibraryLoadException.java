package net.flectone.pulse.exception;

/**
 * Thrown when a library the plugin downloads at runtime cannot be resolved, fetched or loaded.
 *
 * @author TheFaser
 */
public class LibraryLoadException extends RuntimeException {

    /**
     * Creates the exception with a message of its own.
     *
     * @param message what went wrong
     */
    public LibraryLoadException(String message) {
        super(message);
    }

    /**
     * Creates the exception around the failure that broke the load.
     *
     * @param message what went wrong
     * @param cause the underlying failure
     */
    public LibraryLoadException(String message, Throwable cause) {
        super(message, cause);
    }

}
