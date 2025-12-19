package net.flectone.pulse.exception;

public class FileWriteException extends RuntimeException {

    public FileWriteException(String file, Throwable cause) {
        super("Failed to write " + file + "\n" + cause.getMessage());
    }

}

