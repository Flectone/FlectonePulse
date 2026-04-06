package net.flectone.pulse.exception;

import net.flectone.pulse.util.logging.FLogger;

public class InitException extends RuntimeException {

    public InitException(String error) {
        super(FLogger.ERROR_MESSAGE_REPORT + "\nError message: " + error);
    }

}
