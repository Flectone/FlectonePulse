package net.flectone.pulse.exception;

/**
 * Thrown when a class offered as a listener cannot be registered, because it is not a listener
 * at all or one of its handler methods has the wrong signature.
 *
 * @author TheFaser
 */
public class ListenerRegistrationException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message what is wrong with the listener
     */
    public ListenerRegistrationException(String message) {
        super(message);
    }

}
