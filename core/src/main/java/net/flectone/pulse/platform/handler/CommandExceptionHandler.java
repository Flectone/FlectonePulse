package net.flectone.pulse.platform.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.util.file.FileFacade;
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
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CommandExceptionHandler {

    private final FileFacade fileFacade;
    private final EventDispatcher eventDispatcher;
    private final MessagePipeline messagePipeline;
    private final FLogger fLogger;

    public void handleArgumentParseException(ExceptionContext<FPlayer, ArgumentParseException> context) {
        FPlayer fPlayer = context.context().sender();

        Localization.Command.Exception localizationException = fileFacade.localization(fPlayer)
                .command().exception();

        Throwable throwable = context.exception().getCause();
        String message = switch (throwable) {
            case BooleanParser.BooleanParseException e -> Strings.CS.replace(
                    localizationException.parseBoolean(), "<input>", e.input()
            );
            case NumberParseException e -> Strings.CS.replace(
                    localizationException.parseNumber(), "<input>", e.input()
            );
            case DurationParser.DurationParseException e -> Strings.CS.replace(
                    localizationException.parseNumber(), "<input>", e.input()
            );
            case StringParser.StringParseException e -> Strings.CS.replace(
                    localizationException.parseString(), "<input>", e.input()
            );
            default -> Strings.CS.replace(
                    localizationException.parseUnknown(), "<input>", String.valueOf(throwable.getMessage())
            );
        };

        send(fPlayer, messagePipeline.builder(fPlayer, message).build());
    }

    public void handleInvalidSyntaxException(ExceptionContext<FPlayer, InvalidSyntaxException> context) {
        FPlayer fPlayer = context.context().sender();

        String correctSyntax = context.exception().correctSyntax();
        String message = StringUtils.replaceEach(
                fileFacade.localization(fPlayer).command().exception().syntax(),
                new String[]{"<correct_syntax>", "<command>"},
                new String[]{correctSyntax, String.valueOf(correctSyntax.split(" ")[0])}
        );

        send(fPlayer, messagePipeline.builder(fPlayer, message).build());
    }

    public void handleNoPermissionException(ExceptionContext<FPlayer, NoPermissionException> context) {
        FPlayer fPlayer = context.context().sender();

        String message = fileFacade.localization(fPlayer)
                .command().exception().permission();

        send(fPlayer, messagePipeline.builder(fPlayer, message).build());
    }

    public void handleCommandExecutionException(ExceptionContext<FPlayer, CommandExecutionException> context) {
        // send logs to console
        fLogger.warning(context.exception());

        FPlayer fPlayer = context.context().sender();

        String message = Strings.CS.replace(
                fileFacade.localization(fPlayer).command().exception().execution(),
                "<exception>",
                context.exception().getMessage()
        );

        send(fPlayer, messagePipeline.builder(fPlayer, message).build());
    }

    private void send(FPlayer fPlayer, Component component) {
        eventDispatcher.dispatch(new MessageSendEvent(MessageType.ERROR, fPlayer, component));
    }
}
