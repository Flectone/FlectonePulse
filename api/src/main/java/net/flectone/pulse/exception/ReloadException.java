package net.flectone.pulse.exception;

/**
 * Thrown when a reload fails. It is checked so callers have to decide what to show the user.
 * @author TheFaser
 */
public class ReloadException extends Exception {

    /**
     * Creates the exception around the failure that broke the reload.
     *
     * @param cause what went wrong
     */
    public ReloadException(Throwable cause) {
        super(cause);
    }

}