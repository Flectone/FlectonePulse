package net.flectone.pulse.exception;

/**
 * Thrown to indicate that the requested database operation is not supported
 * by the current SQL dialect.
 *
 * @author TheFaser
 * @since 1.10.0
 */
public class UnsupportedDatabaseOperationException extends RuntimeException {

    public UnsupportedDatabaseOperationException() {
        super("This operation is not supported by the base sql interface");
    }

}