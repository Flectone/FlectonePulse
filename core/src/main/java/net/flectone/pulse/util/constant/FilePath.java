package net.flectone.pulse.util.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FilePath {

    COMMAND("command.yml"),
    CONFIG("config.yml"),
    INTEGRATION("integration.yml"),
    LOCALIZATION_FOLDER("localizations/"),
    ENGLISH_LOCALIZATION(LOCALIZATION_FOLDER.path + DefaultLocalization.ENGLISH.getName() + ".yml"),
    RUSSIAN_LOCALIZATION(LOCALIZATION_FOLDER.path + DefaultLocalization.RUSSIAN.getName() + ".yml"),
    MESSAGE("message.yml"),
    PERMISSION("permission.yml");

    private final String path;

}
