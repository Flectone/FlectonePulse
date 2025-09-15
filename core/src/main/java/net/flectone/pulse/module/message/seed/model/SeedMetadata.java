package net.flectone.pulse.module.message.seed.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

@Getter
@SuperBuilder
public class SeedMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @NonNull
    private final String seed;

    @NonNull
    private final MinecraftTranslationKey translationKey;

}
