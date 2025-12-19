package net.flectone.pulse.util.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DefaultLocalization {

    ENGLISH("en_us"),
    RUSSIAN("ru_ru");

    private final String name;

}
