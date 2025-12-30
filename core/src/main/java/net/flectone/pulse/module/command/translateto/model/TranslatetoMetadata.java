package net.flectone.pulse.module.command.translateto.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Getter
@SuperBuilder
public class TranslatetoMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    @NonNull
    private final String targetLanguage;

    @NonNull
    private final String messageToTranslate;

}
