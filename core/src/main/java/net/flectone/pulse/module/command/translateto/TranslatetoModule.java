package net.flectone.pulse.module.command.translateto;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.translateto.model.TranslatetoMetadata;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Singleton
public class TranslatetoModule extends AbstractModuleCommand<Localization.Command.Translateto> {

    private final FileResolver fileResolver;
    private final CommandParserProvider commandParserProvider;
    private final IntegrationModule integrationModule;
    private final Provider<TranslateModule> translateModuleProvider;

    @Inject
    public TranslatetoModule(FileResolver fileResolver,
                             CommandParserProvider commandParserProvider,
                             IntegrationModule integrationModule,
                             Provider<TranslateModule> translateModuleProvider) {
        super(MessageType.COMMAND_TRANSLATETO);

        this.fileResolver = fileResolver;
        this.commandParserProvider = commandParserProvider;
        this.integrationModule = integrationModule;
        this.translateModuleProvider = translateModuleProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createCooldown(config().getCooldown(), permission().getCooldownBypass());
        createSound(config().getSound(), permission().getSound());

        String promptLanguage = addPrompt(0, Localization.Command.Prompt::getLanguage);
        String promptMessage = addPrompt(1, Localization.Command.Prompt::getMessage);
        registerCommand(manager -> manager
                .required(promptLanguage + " main", commandParserProvider.singleMessageParser(), languageSuggestion())
                .required(promptLanguage + " target", commandParserProvider.singleMessageParser(), languageSuggestion())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
                .permission(permission().getName())
        );
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> languageSuggestion() {
        return (context, input) -> config().getLanguages()
                .stream()
                .map(Suggestion::suggestion)
                .toList();
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String promptLanguage = getPrompt(0);
        String mainLang = commandContext.get(promptLanguage + " main");
        String targetLang = commandContext.get(promptLanguage + " target");

        String message = getArgument(commandContext, 1);

        String messageToTranslate = translateModuleProvider.get().getMessage(message);
        if (StringUtils.isEmpty(messageToTranslate)) {
            messageToTranslate = message;
        }

        String translatedMessage = translate(fPlayer, mainLang, targetLang, messageToTranslate);
        if (translatedMessage.isEmpty()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Translateto::getNullOrError)
                    .build()
            );

            return;
        }

        String finalMessageToTranslate = messageToTranslate;
        sendMessage(TranslatetoMetadata.<Localization.Command.Translateto>builder()
                .sender(fPlayer)
                .format(replaceLanguage(targetLang))
                .targetLanguage(targetLang)
                .messageToTranslate(messageToTranslate)
                .range(config().getRange())
                .destination(config().getDestination())
                .message(translatedMessage)
                .sound(getModuleSound())
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeString(targetLang);
                    dataOutputStream.writeString(message);
                    dataOutputStream.writeString(finalMessageToTranslate);
                })
                .integration(string -> Strings.CS.replace(string, "<language>", targetLang))
                .build()
        );
    }

    @Override
    public Command.Translateto config() {
        return fileResolver.getCommand().getTranslateto();
    }

    @Override
    public Permission.Command.Translateto permission() {
        return fileResolver.getPermission().getCommand().getTranslateto();
    }

    @Override
    public Localization.Command.Translateto localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getTranslateto();
    }

    public Function<Localization.Command.Translateto, String> replaceLanguage(String targetLang) {
        return message -> Strings.CS.replace(message.getFormat(), "<language>", targetLang);
    }

    public String translate(FPlayer fPlayer, String source, String target, String text) {
        return switch (config().getService()) {
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
            uc.setRequestProperty("User-Agent", "Mozilla/5.0");

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
