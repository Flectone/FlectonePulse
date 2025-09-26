package net.flectone.pulse.module.message.deop.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

@Getter
@SuperBuilder
public class DeopMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @NonNull
    private final FEntity target;

    @NonNull
    private final MinecraftTranslationKey translationKey;

}
