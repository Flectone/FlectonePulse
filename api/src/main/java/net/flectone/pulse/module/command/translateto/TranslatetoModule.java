package net.flectone.pulse.module.command.translateto;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;

/**
 * The /translateto command, which translates a message into another language.
 * @author TheFaser
 */
public interface TranslatetoModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Translateto config();

    @Override
    Permission.Command.Translateto permission();

    @Override
    Localization.Command.Translateto localization(FPlayer fPlayer);

    /**
     * The display name of a language in the reader's own language.
     *
     * @param fPlayer the reader
     * @param targetLang the language code
     * @return the language name
     */
    String replaceLanguage(FPlayer fPlayer, String targetLang);

    /**
     * Translates text using whichever translation integration is enabled.
     *
     * @param fPlayer who asked
     * @param source the language to translate from, or an empty string to detect it
     * @param target the language to translate into
     * @param text the text
     * @return the translation, or the original text if no service could do it
     */
    String translate(FPlayer fPlayer, String source, String target, String text);

    /**
     * Translates text through Google, the fallback when no integration is configured.
     *
     * @param source the language to translate from
     * @param lang the language to translate into
     * @param text the text
     * @return the translation, or the original text on failure
     */
    String googleTranslate(String source, String lang, String text);

}
