package net.flectone.pulse.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.sender.MessageSender;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.exception.handling.ExceptionContext;
import org.incendo.cloud.exception.parsing.NumberParseException;
import org.incendo.cloud.parser.standard.BooleanParser;
import org.incendo.cloud.parser.standard.DurationParser;
import org.incendo.cloud.parser.standard.StringParser;

@Singleton
public class CommandExceptionHandler {

    private final FileResolver fileResolver;
    private final MessageSender messageSender;
    private final MessagePipeline messagePipeline;
    private final FLogger fLogger;

    @Inject
    public CommandExceptionHandler(FileResolver fileResolver,
                                   MessageSender messageSender,
                                   MessagePipeline messagePipeline,
                                   FLogger fLogger) {
        this.fileResolver = fileResolver;
        this.messageSender = messageSender;
        this.messagePipeline = messagePipeline;
        this.fLogger = fLogger;
    }

    public void handleArgumentParseException(ExceptionContext<FPlayer, ArgumentParseException> context) {
        FPlayer fPlayer = context.context().sender();

        Localization.Command.Exception localizationException = fileResolver.getLocalization(fPlayer)
                .getCommand().getException();

        String message;
        Throwable throwable = context.exception().getCause();
        if (throwable instanceof BooleanParser.BooleanParseException e) {
            message = localizationException.getParseBoolean()
                    .replace("<input>", e.input());
        } else if (throwable instanceof NumberParseException e) {
            message = localizationException.getParseNumber()
                    .replace("<input>", e.input());
        } else if (throwable instanceof DurationParser.DurationParseException e) {
            message = localizationException.getParseNumber()
                    .replace("<input>", e.input());
        } else if (throwable instanceof StringParser.StringParseException e) {
            message = localizationException.getParseString()
                    .replace("<input>", e.input());
        } else {
            message = localizationException.getParseUnknown()
                    .replace("<input>", throwable.getMessage());
        }

        Component componentMessage = messagePipeline.builder(fPlayer, message)
                .player(false)
                .build();

        messageSender.sendMessage(fPlayer, componentMessage);
    }

    public void handleInvalidSyntaxException(ExceptionContext<FPlayer, InvalidSyntaxException> context) {
        FPlayer fPlayer = context.context().sender();

        String correctSyntax = context.exception().correctSyntax();
        String message = fileResolver.getLocalization(fPlayer)
                .getCommand().getException().getSyntax()
                .replace("<correct_syntax>", correctSyntax)
                .replace("<command>", correctSyntax.split(" ")[0]);

        Component componentMessage = messagePipeline.builder(fPlayer, message)
                .player(false)
                .build();

        messageSender.sendMessage(fPlayer, componentMessage);
    }

    public void handleNoPermissionException(ExceptionContext<FPlayer, NoPermissionException> context) {
        FPlayer fPlayer = context.context().sender();

        String message = fileResolver.getLocalization(fPlayer)
                .getCommand().getException().getPermission();

        Component componentMessage = messagePipeline.builder(fPlayer, message)
                .player(false)
                .build();

        messageSender.sendMessage(fPlayer, componentMessage);
    }

    public void handleCommandExecutionException(ExceptionContext<FPlayer, CommandExecutionException> context) {
        // send logs to console
        if (!context.context().sender().isUnknown()) {
            fLogger.warning(context.exception());
        }

        FPlayer fPlayer = context.context().sender();

        String message = fileResolver.getLocalization(fPlayer)
                .getCommand().getException().getExecution()
                .replace("<exception>", context.exception().getMessage());

        Component componentMessage = messagePipeline.builder(fPlayer, message)
                .player(false)
                .build();

        messageSender.sendMessage(fPlayer, componentMessage);
    }
}
