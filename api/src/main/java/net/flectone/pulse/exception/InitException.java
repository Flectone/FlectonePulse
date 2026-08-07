package net.flectone.pulse.exception;

import net.flectone.pulse.util.logging.FLogger;

/**
 * Thrown when the plugin cannot finish starting up. The message asks the user to report the problem.
 * @author TheFaser
 */
public class InitException extends RuntimeException {

    /**
     * Creates the exception with a report hint in front of the given message.
     *
     * @param error what went wrong
     */
    public InitException(String error) {
        super(FLogger.ERROR_MESSAGE_REPORT + "\nError message: " + error);
    }

}
