package net.flectone.pulse.exception;

/**
 * Thrown when the database cannot be written to a dump file or read back from one.
 *
 * @author TheFaser
 */
public class DatabaseDumpException extends RuntimeException {

    /**
     * Creates the exception with a message of its own.
     *
     * @param message what went wrong
     */
    public DatabaseDumpException(String message) {
        super(message);
    }

    /**
     * Creates the exception around the failure that broke the dump.
     *
     * @param message what went wrong
     * @param cause the underlying failure
     */
    public DatabaseDumpException(String message, Throwable cause) {
        super(message, cause);
    }

}
