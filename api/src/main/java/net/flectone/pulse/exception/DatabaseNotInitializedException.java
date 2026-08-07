package net.flectone.pulse.exception;

/**
 * Thrown when the database is used before a connection has been opened.
 *
 * @author TheFaser
 */
public class DatabaseNotInitializedException extends RuntimeException {

    /**
     * Creates the exception.
     */
    public DatabaseNotInitializedException() {
        super("Database connection has not been opened yet");
    }

}
