package net.flectone.pulse.module.command.translateto.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.model.event.EventMetadata;

@Getter
@SuperBuilder
public class TranslatetoMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @NonNull
    private final String targetLanguage;

    @NonNull
    private final String messageToTranslate;

}
