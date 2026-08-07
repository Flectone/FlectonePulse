package net.flectone.pulse.module.integration.deepl;

import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;

/**
 * Translates messages through DeepL.
 * @author TheFaser
 */
public interface DeeplModule extends ModuleSimple {

    @Override
    ModuleName name();

    @Override
    Integration.Deepl config();

    @Override
    Permission.Integration.Deepl permission();

    /**
     * Translates text through DeepL.
     *
     * @param sender who asked
     * @param source the language to translate from, or an empty string to detect it
     * @param target the language to translate into
     * @param text the text
     * @return the translation, or the original text on failure
     */
    String translate(FPlayer sender, String source, String target, String text);

}
