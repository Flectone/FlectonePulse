package net.flectone.pulse.exception;

import java.io.IOException;

public class ProxyMessageCreateException extends RuntimeException {

    public ProxyMessageCreateException(IOException e) {
        super("Failed to create proxy message: " + e);
    }

}
