package net.flectone.pulse.module.integration.yandex;

import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;

/**
 * Translates messages through Yandex Translate.
 * @author TheFaser
 */
public interface YandexModule extends ModuleSimple {

    @Override
    Integration.Yandex config();

    @Override
    Permission.Integration.Yandex permission();

    /**
     * Translates text through Yandex.
     *
     * @param sender who asked
     * @param source the language to translate from, or an empty string to detect it
     * @param target the language to translate into
     * @param text the text
     * @return the translation, or the original text on failure
     */
    String translate(FPlayer sender, String source, String target, String text);

}
