package net.flectone.pulse.platform.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
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
    private final SocialService socialService;
    private final FLogger fLogger;

    public void handleArgumentParseException(ExceptionContext<FPlayer, ArgumentParseException> context) {
        FPlayer fPlayer = context.context().sender();

        Localization.Command.Exception localizationException = fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE))
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

        send(fPlayer, messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .message(message)
                .build()
        ));
    }

    public void handleInvalidSyntaxException(ExceptionContext<FPlayer, InvalidSyntaxException> context) {
        FPlayer fPlayer = context.context().sender();

        String correctSyntax = context.exception().correctSyntax();

        send(fPlayer, messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .message(StringUtils.replaceEach(
                        fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().exception().syntax(),
                        new String[]{"<correct_syntax>", "<command>"},
                        new String[]{correctSyntax, String.valueOf(correctSyntax.split(" ")[0])}
                ))
                .build()
        ));
    }

    public void handleNoPermissionException(ExceptionContext<FPlayer, NoPermissionException> context) {
        FPlayer fPlayer = context.context().sender();

        send(fPlayer, messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .message(fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().exception().permission())
                .build()
        ));
    }

    public void handleCommandExecutionException(ExceptionContext<FPlayer, CommandExecutionException> context) {
        // send logs to console
        fLogger.warning(context.exception());

        FPlayer fPlayer = context.context().sender();

        send(fPlayer, messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .message(Strings.CS.replace(
                        fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().exception().execution(),
                        "<exception>",
                        context.exception().getMessage()
                ))
                .build()
        ));
    }

    private void send(FPlayer fPlayer, Component component) {
        eventDispatcher.dispatch(new MessageSendEvent(ModuleName.ERROR, fPlayer, component));
    }
}
