package net.flectone.pulse.config.setting;

import net.flectone.pulse.config.Localization;

/**
 * The wording a punishment list command uses for its header, its rows and its paging buttons.
 * @author TheFaser
 */
public interface ModerationListLocalizationSetting {

    String empty();

    String nullPage();

    String nullPlayer();

    Localization.ListTypeMessage global();

    Localization.ListTypeMessage player();

}
