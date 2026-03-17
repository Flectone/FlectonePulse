package net.flectone.pulse.module.command.translateto;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.translateto.model.TranslatetoMetadata;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.util.WebUtil;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;
import org.jspecify.annotations.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TranslatetoModule implements ModuleCommand<Localization.Command.Translateto> {

    private final FileFacade fileFacade;
    private final CommandParserProvider commandParserProvider;
    private final IntegrationModule integrationModule;
    private final Provider<TranslateModule> translateModuleProvider;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;

    @Override
    public void onEnable() {
        String promptLanguage = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::language);
        String promptMessage = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::message);
        commandModuleController.registerCommand(this, manager -> manager
                .required(promptLanguage + " main", commandParserProvider.singleMessageParser(), languageSuggestion())
                .required(promptLanguage + " target", commandParserProvider.singleMessageParser(), languageSuggestion())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
                .permission(permission().name())
        );
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> languageSuggestion() {
        return (context, input) -> config().languages()
                .stream()
                .map(Suggestion::suggestion)
                .toList();
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String promptLanguage = commandModuleController.getPrompt(this, 0);
        String mainLang = commandContext.get(promptLanguage + " main");
        String targetLang = commandContext.get(promptLanguage + " target");

        String message = commandModuleController.getArgument(this, commandContext, 1);

        String messageToTranslate = translateModuleProvider.get().getMessage(message);
        if (StringUtils.isEmpty(messageToTranslate)) {
            messageToTranslate = message;
        }

        String translatedMessage = translate(fPlayer, mainLang, targetLang, messageToTranslate);
        if (translatedMessage.isEmpty()) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Translateto>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Translateto::nullOrError)
                    .build()
            );

            return;
        }

        String finalMessageToTranslate = messageToTranslate;
        messageDispatcher.dispatch(this, TranslatetoMetadata.<Localization.Command.Translateto>builder()
                .base(EventMetadata.<Localization.Command.Translateto>builder()
                        .sender(fPlayer)
                        .format(replaceLanguage(targetLang))
                        .range(config().range())
                        .destination(config().destination())
                        .message(translatedMessage)
                        .sound(soundOrThrow())
                        .proxy(dataOutputStream -> {
                            dataOutputStream.writeString(targetLang);
                            dataOutputStream.writeString(message);
                            dataOutputStream.writeString(finalMessageToTranslate);
                        })
                        .integration(string -> Strings.CS.replace(string, "<language>", targetLang))
                        .build()
                )
                .targetLanguage(targetLang)
                .messageToTranslate(messageToTranslate)
                .build()
        );
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_TRANSLATETO;
    }

    @Override
    public Command.Translateto config() {
        return fileFacade.command().translateto();
    }

    @Override
    public Permission.Command.Translateto permission() {
        return fileFacade.permission().command().translateto();
    }

    @Override
    public Localization.Command.Translateto localization(FEntity sender) {
        return fileFacade.localization(sender).command().translateto();
    }

    public Function<Localization.Command.Translateto, String> replaceLanguage(String targetLang) {
        return message -> Strings.CS.replace(message.format(), "<language>", targetLang);
    }

    public String translate(FPlayer fPlayer, String source, String target, String text) {
        return switch (config().service()) {
            case DEEPL -> integrationModule.deeplTranslate(fPlayer, source, target, text);
            case GOOGLE -> googleTranslate(source, target, text);
            case YANDEX -> integrationModule.yandexTranslate(fPlayer, source, target, text);
        };
    }

    public String googleTranslate(String source, String lang, String text) {
        try {
            text = URLEncoder.encode(text, StandardCharsets.UTF_8);
            URL url = new URI("http://translate.googleapis.com/translate_a/single?client=gtx&sl=" + source + "&tl="
                    + lang + "&dt=t&q=" + text + "&ie=UTF-8&oe=UTF-8").toURL();

            URLConnection uc = url.openConnection();
            uc.setRequestProperty("User-Agent", WebUtil.USER_AGENT);

            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                text = inputLine;
            }

            in.close();

            String jsonResponse = text;
            int startIndex = jsonResponse.indexOf("\"") + 1;
            int endIndex = jsonResponse.indexOf("\"", startIndex);

            return jsonResponse.substring(startIndex, endIndex);
        } catch (IOException | URISyntaxException ignored) {
            return "";
        }
    }
}
