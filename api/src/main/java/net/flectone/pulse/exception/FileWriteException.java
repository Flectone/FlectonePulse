package net.flectone.pulse.exception;

/**
 * Thrown when a config or localization file cannot be written to disk.
 * @author TheFaser
 */
public class FileWriteException extends RuntimeException {

    /**
     * Creates the exception, naming the file and quoting the underlying failure.
     *
     * @param file the file that could not be written
     * @param cause what went wrong
     */
    public FileWriteException(String file, Throwable cause) {
        super("Failed to write " + file + "\n" + cause.getMessage());
    }

}

