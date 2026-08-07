package net.flectone.pulse.util.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The languages shipped with the plugin. Any other locale is merged on top of the closest of these.
 * @author TheFaser
 */
@Getter
@AllArgsConstructor
public enum DefaultLocalization {

    ENGLISH("en_us"),
    RUSSIAN("ru_ru");

    private final String name;

}
