package net.flectone.pulse.module.command.translateto;

import lombok.Getter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public abstract class TranslatetoModule extends AbstractModuleCommand<Localization.Command.Translateto> {

    @Getter private final Command.Translateto command;
    @Getter private final Permission.Command.Translateto permission;

    private final CommandUtil commandUtil;
    private final IntegrationModule integrationModule;

    public TranslatetoModule(FileManager fileManager,
                             CommandUtil commandUtil,
                             IntegrationModule integrationModule) {
        super(localization -> localization.getCommand().getTranslateto(), fPlayer -> fPlayer.is(FPlayer.Setting.TRANSLATETO));

        this.commandUtil = commandUtil;
        this.integrationModule = integrationModule;

        command = fileManager.getCommand().getTranslateto();
        permission = fileManager.getPermission().getCommand().getTranslateto();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        String mainLang = commandUtil.getString(0, arguments);
        String targetLang = commandUtil.getString(1, arguments);
        String message = commandUtil.getString(2, arguments);

        String translated = translate(fPlayer, mainLang, targetLang, message);
        if (translated.isEmpty()) {
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
                .message(translated)
                .proxy(output -> {
                    output.writeUTF(targetLang);
                    output.writeUTF(translated);
                })
                .integration(s -> s
                        .replace("<message>", translated)
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

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        getCommand().getAliases().forEach(commandUtil::unregister);

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}
