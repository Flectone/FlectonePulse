package net.flectone.pulse.exception;

import java.io.IOException;

public class YamlWriteException extends IOException {

    public YamlWriteException(String file, Throwable cause) {
        super("Failed to write " + file + "\n" + cause.getMessage());
    }

}

