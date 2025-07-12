package net.flectone.pulse.module.command.translateto;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.NonNull;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;
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

    @Getter private final Command.Translateto command;
    private final Permission.Command.Translateto permission;
    private final CommandRegistry commandRegistry;
    private final IntegrationModule integrationModule;
    private final Provider<TranslateModule> translateModuleProvider;

    @Inject
    public TranslatetoModule(FileResolver fileResolver,
                             CommandRegistry commandRegistry,
                             IntegrationModule integrationModule,
                             Provider<TranslateModule> translateModuleProvider) {
        super(localization -> localization.getCommand().getTranslateto(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.TRANSLATETO));

        this.command = fileResolver.getCommand().getTranslateto();
        this.permission = fileResolver.getPermission().getCommand().getTranslateto();
        this.commandRegistry = commandRegistry;
        this.integrationModule = integrationModule;
        this.translateModuleProvider = translateModuleProvider;
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptLanguage = getPrompt().getLanguage();
        String promptMessage = getPrompt().getMessage();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .required(promptLanguage + " main", commandRegistry.singleMessageParser(), languageSuggestion())
                        .required(promptLanguage + " target", commandRegistry.singleMessageParser(), languageSuggestion())
                        .required(promptMessage, commandRegistry.nativeMessageParser())
                        .permission(permission.getName())
                        .handler(this)
        );

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
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
        if (checkModulePredicates(fPlayer)) return;

        String promptLanguage = getPrompt().getLanguage();
        String mainLang = commandContext.get(promptLanguage + " main");
        String targetLang = commandContext.get(promptLanguage + " target");

        String promptMessage = getPrompt().getMessage();
        String message = commandContext.get(promptMessage);

        String messageToTranslate = translateModuleProvider.get().getMessage(message);
        if (messageToTranslate == null) {
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
                .tag(MessageTag.COMMAND_TRANSLATETO)
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
