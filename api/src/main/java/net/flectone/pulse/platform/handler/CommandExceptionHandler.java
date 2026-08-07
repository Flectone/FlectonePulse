package net.flectone.pulse.platform.handler;

import net.flectone.pulse.model.entity.FPlayer;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.exception.handling.ExceptionContext;

/**
 * Turns the command framework's failures into messages a player can read, in their own language.
 * @author TheFaser
 */
public interface CommandExceptionHandler {

    /**
     * Reports an argument that could not be parsed.
     *
     * @param context the failure and who caused it
     */
    void handleArgumentParseException(ExceptionContext<FPlayer, ArgumentParseException> context);

    /**
     * Reports a command used with the wrong syntax, showing the correct form.
     *
     * @param context the failure and who caused it
     */
    void handleInvalidSyntaxException(ExceptionContext<FPlayer, InvalidSyntaxException> context);

    /**
     * Reports a command the player is not allowed to run.
     *
     * @param context the failure and who caused it
     */
    void handleNoPermissionException(ExceptionContext<FPlayer, NoPermissionException> context);

    /**
     * Reports a command that threw while running, and logs the cause.
     *
     * @param context the failure and who caused it
     */
    void handleCommandExecutionException(ExceptionContext<FPlayer, CommandExecutionException> context);

}
