package net.flectone.pulse.exception;

import java.io.IOException;

public class YamlReadException extends IOException {

    public YamlReadException(String file, Throwable cause) {
        super("Failed to read " + file + "\n" + cause.getMessage());
    }

}
