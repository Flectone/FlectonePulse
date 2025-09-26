package net.flectone.pulse.module.message.clear.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

@Getter
@SuperBuilder
public class ClearMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @NonNull
    private final Clear clear;

    @NonNull
    private final MinecraftTranslationKey translationKey;

}
