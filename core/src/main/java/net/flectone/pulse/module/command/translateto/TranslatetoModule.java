package net.flectone.pulse.module.command.translateto;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.DisableSource;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Singleton
public class TranslatetoModule extends AbstractModuleCommand<Localization.Command.Translateto> {

    private final Command.Translateto command;
    private final Permission.Command.Translateto permission;
    private final CommandParserProvider commandParserProvider;
    private final IntegrationModule integrationModule;
    private final Provider<TranslateModule> translateModuleProvider;

    @Inject
    public TranslatetoModule(FileResolver fileResolver,
                             CommandParserProvider commandParserProvider,
                             IntegrationModule integrationModule,
                             Provider<TranslateModule> translateModuleProvider) {
        super(localization -> localization.getCommand().getTranslateto(), Command::getTranslateto, fPlayer -> fPlayer.isSetting(FPlayer.Setting.TRANSLATETO));

        this.command = fileResolver.getCommand().getTranslateto();
        this.permission = fileResolver.getPermission().getCommand().getTranslateto();
        this.commandParserProvider = commandParserProvider;
        this.integrationModule = integrationModule;
        this.translateModuleProvider = translateModuleProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptLanguage = addPrompt(0, Localization.Command.Prompt::getLanguage);
        String promptMessage = addPrompt(1, Localization.Command.Prompt::getMessage);
        registerCommand(manager -> manager
                .required(promptLanguage + " main", commandParserProvider.singleMessageParser(), languageSuggestion())
                .required(promptLanguage + " target", commandParserProvider.singleMessageParser(), languageSuggestion())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
                .permission(permission.getName())
        );

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableSource.YOU));
        addPredicate(this::checkMute);
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> languageSuggestion() {
        return (context, input) -> command.getLanguages()
                .stream()
                .map(Suggestion::suggestion)
                .toList();
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer)) return;

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
            builder(fPlayer)
                    .format(Localization.Command.Translateto::getNullOrError)
                    .sendBuilt();
            return;
        }

        builder(fPlayer)
                .range(command.getRange())
                .destination(command.getDestination())
                .tag(MessageType.COMMAND_TRANSLATETO)
                .format(replaceLanguage(targetLang))
                .message(translatedMessage)
                .proxy(output -> {
                    output.writeUTF(targetLang);
                    output.writeUTF(translatedMessage);
                })
                .integration(s -> s
                        .replace("<message>", translatedMessage)
                        .replace("<language>", targetLang)
                )
                .sound(getSound())
                .sendBuilt();
    }

    public Function<Localization.Command.Translateto, String> replaceLanguage(String targetLang) {
        return message -> message.getFormat().replace("<language>", targetLang);
    }

    public String translate(FPlayer fPlayer, String source, String target, String text) {
        return switch (command.getService()) {
            case DEEPL -> integrationModule.deeplTranslate(fPlayer, source, target, text);
            case GOOGLE -> googleTranslate(source, target, text);
            case YANDEX -> integrationModule.yandexTranslate(fPlayer, source, target, text);
        };
    }

    public String googleTranslate(String source, String lang, String text) {
        try {
            text = URLEncoder.encode(text, StandardCharsets.UTF_8);
            URL url = new URL("http://translate.googleapis.com/translate_a/single?client=gtx&sl=" + source + "&tl="
                    + lang + "&dt=t&q=" + text + "&ie=UTF-8&oe=UTF-8");

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

        } catch (IOException ignored) {}

        return "";
    }
}
