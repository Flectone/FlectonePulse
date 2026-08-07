package net.flectone.pulse.module.command.flectonepulse.web.exception;

public class EmptyHostException extends IllegalArgumentException {

    public EmptyHostException() {
        super("The host parameter cannot be empty and must be configured in config.yml");
    }

}
