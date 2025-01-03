package net.flectone.pulse.module.command.translateto;

import lombok.Getter;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
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

    @Getter
    private final Command.Translateto command;
    @Getter
    private final Permission.Command.Translateto permission;

    private final CommandUtil commandUtil;

    public TranslatetoModule(FileManager fileManager,
                             CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getTranslateto(), fPlayer -> fPlayer.is(FPlayer.Setting.TRANSLATETO));

        this.commandUtil = commandUtil;

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

        String translated = translate(mainLang, targetLang, message);
        if (translated.isEmpty() || message.equals(translated)) {
            builder(fPlayer)
                    .format(Localization.Command.Translateto::getNullOrError)
                    .sendBuilt();
            return;
        }

        builder(fPlayer)
                .range(command.getRange())
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

    public String translate(String sourceLang, String targetLang, String msg) {
        try {
            msg = URLEncoder.encode(msg, StandardCharsets.UTF_8);
            URL url = new URL("http://translate.googleapis.com/translate_a/single?client=gtx&sl=" + sourceLang + "&tl="
                    + targetLang + "&dt=t&q=" + msg + "&ie=UTF-8&oe=UTF-8");

            URLConnection uc = url.openConnection();
            uc.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                msg = inputLine;
            }

            in.close();

            String jsonResponse = msg;
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
