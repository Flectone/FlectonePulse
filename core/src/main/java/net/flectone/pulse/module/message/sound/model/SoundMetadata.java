package net.flectone.pulse.module.message.sound.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

@Getter
@SuperBuilder
public class SoundMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @NonNull
    private final Sound metaSound;

    @NonNull
    private final MinecraftTranslationKey translationKey;

}
