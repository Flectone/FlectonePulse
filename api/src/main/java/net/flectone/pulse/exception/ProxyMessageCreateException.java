package net.flectone.pulse.exception;

import java.io.IOException;

/**
 * Thrown when a message bound for another server cannot be encoded or decoded.
 * @author TheFaser
 */
public class ProxyMessageCreateException extends RuntimeException {

    /**
     * Creates the exception from the failure that broke the stream.
     *
     * @param e what went wrong
     */
    public ProxyMessageCreateException(IOException e) {
        super("Failed to create proxy message: " + e);
    }

}
