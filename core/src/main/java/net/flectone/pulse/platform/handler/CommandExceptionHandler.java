package net.flectone.pulse.platform.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.SenderToReceiverMessageEvent;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
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
    private final EventDispatcher eventDispatcher;
    private final MessagePipeline messagePipeline;
    private final FLogger fLogger;

    @Inject
    public CommandExceptionHandler(FileResolver fileResolver,
                                   EventDispatcher eventDispatcher,
                                   MessagePipeline messagePipeline,
                                   FLogger fLogger) {
        this.fileResolver = fileResolver;
        this.eventDispatcher = eventDispatcher;
        this.messagePipeline = messagePipeline;
        this.fLogger = fLogger;
    }

    public void handleArgumentParseException(ExceptionContext<FPlayer, ArgumentParseException> context) {
        FPlayer fPlayer = context.context().sender();

        Localization.Command.Exception localizationException = fileResolver.getLocalization(fPlayer)
                .getCommand().getException();

        Throwable throwable = context.exception().getCause();
        String message = switch (throwable) {
            case BooleanParser.BooleanParseException e -> Strings.CS.replace(
                    localizationException.getParseBoolean(), "<input>", e.input()
            );
            case NumberParseException e -> Strings.CS.replace(
                    localizationException.getParseNumber(), "<input>", e.input()
            );
            case DurationParser.DurationParseException e -> Strings.CS.replace(
                    localizationException.getParseNumber(), "<input>", e.input()
            );
            case StringParser.StringParseException e -> Strings.CS.replace(
                    localizationException.getParseString(), "<input>", e.input()
            );
            default -> Strings.CS.replace(
                    localizationException.getParseUnknown(), "<input>", String.valueOf(throwable.getMessage())
            );
        };

        send(fPlayer, messagePipeline.builder(fPlayer, message).build());
    }

    public void handleInvalidSyntaxException(ExceptionContext<FPlayer, InvalidSyntaxException> context) {
        FPlayer fPlayer = context.context().sender();

        String correctSyntax = context.exception().correctSyntax();
        String message = StringUtils.replaceEach(
                fileResolver.getLocalization(fPlayer).getCommand().getException().getSyntax(),
                new String[]{"<correct_syntax>", "<command>"},
                new String[]{correctSyntax, String.valueOf(correctSyntax.split(" ")[0])}
        );

        send(fPlayer, messagePipeline.builder(fPlayer, message).build());
    }

    public void handleNoPermissionException(ExceptionContext<FPlayer, NoPermissionException> context) {
        FPlayer fPlayer = context.context().sender();

        String message = fileResolver.getLocalization(fPlayer)
                .getCommand().getException().getPermission();

        send(fPlayer, messagePipeline.builder(fPlayer, message).build());
    }

    public void handleCommandExecutionException(ExceptionContext<FPlayer, CommandExecutionException> context) {
        // send logs to console
        fLogger.warning(context.exception());

        FPlayer fPlayer = context.context().sender();

        String message = Strings.CS.replace(
                fileResolver.getLocalization(fPlayer).getCommand().getException().getExecution(),
                "<exception>",
                context.exception().getMessage()
        );

        send(fPlayer, messagePipeline.builder(fPlayer, message).build());
    }

    private void send(FPlayer fPlayer, Component component) {
        eventDispatcher.dispatch(new SenderToReceiverMessageEvent(MessageType.ERROR, fPlayer, component));
    }
}
