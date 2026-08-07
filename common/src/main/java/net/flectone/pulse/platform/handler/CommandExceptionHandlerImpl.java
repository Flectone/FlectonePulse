package net.flectone.pulse.platform.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.service.SocialService;
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
public class CommandExceptionHandlerImpl implements CommandExceptionHandler {

    private final FileFacade fileFacade;
    private final MessageDispatcher messageDispatcher;
    private final SocialService socialService;
    private final FLogger fLogger;

    @Override
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

        messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                .messageContext(fResolver -> MessageContext.builder()
                        .sender(fPlayer)
                        .receiver(fResolver)
                        .message(message)
                        .build()
                )
                .build()
        );
    }

    @Override
    public void handleInvalidSyntaxException(ExceptionContext<FPlayer, InvalidSyntaxException> context) {
        FPlayer fPlayer = context.context().sender();

        String correctSyntax = context.exception().correctSyntax();

        messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                .messageContext(fResolver -> MessageContext.builder()
                        .sender(fPlayer)
                        .receiver(fResolver)
                        .message(StringUtils.replaceEach(
                                fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().exception().syntax(),
                                new String[]{"<correct_syntax>", "<command>"},
                                new String[]{correctSyntax, String.valueOf(correctSyntax.split(" ")[0])}
                        ))
                        .flags(
                                new MessageFlag[]{MessageFlag.PLAYER_NAME, MessageFlag.REMOVE_DISABLED_TAGS},
                                new boolean[]{false, false}
                        )
                        .build()
                )
                .build()
        );
    }

    @Override
    public void handleNoPermissionException(ExceptionContext<FPlayer, NoPermissionException> context) {
        FPlayer fPlayer = context.context().sender();

        messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                .messageContext(fResolver -> MessageContext.builder()
                        .sender(fPlayer)
                        .receiver(fResolver)
                        .message(fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().exception().permission())
                        .build()
                )
                .build()
        );
    }

    @Override
    public void handleCommandExecutionException(ExceptionContext<FPlayer, CommandExecutionException> context) {
        // send logs to console
        fLogger.warning(context.exception());

        FPlayer fPlayer = context.context().sender();

        messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                .messageContext(fResolver -> MessageContext.builder()
                        .sender(fPlayer)
                        .receiver(fResolver)
                        .message(Strings.CS.replace(
                                fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().exception().execution(),
                                "<exception>",
                                context.exception().getMessage()
                        ))
                        .build()
                )
                .build()
        );
    }
}
