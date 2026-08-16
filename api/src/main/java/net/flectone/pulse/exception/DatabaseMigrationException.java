package net.flectone.pulse.exception;

/**
 * Thrown when a database file cannot be moved to the format the current driver works with.
 *
 * @author TheFaser
 */
public class DatabaseMigrationException extends RuntimeException {

    /**
     * Creates the exception with a message of its own.
     *
     * @param message what went wrong
     */
    public DatabaseMigrationException(String message) {
        super(message);
    }

    /**
     * Creates the exception around the failure that broke the migration.
     *
     * @param message what went wrong
     * @param cause the underlying failure
     */
    public DatabaseMigrationException(String message, Throwable cause) {
        super(message, cause);
    }

}
