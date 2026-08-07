package net.flectone.pulse.exception;

/**
 * Thrown when a config or localization file cannot be read or parsed.
 * @author TheFaser
 */
public class FileLoadException extends RuntimeException {

    /**
     * Creates the exception, naming the file and quoting the underlying failure.
     *
     * @param file the file that could not be read
     * @param cause what went wrong
     */
    public FileLoadException(String file, Throwable cause) {
        super("Failed to read " + file + "\n" + cause.getMessage());
    }

}
