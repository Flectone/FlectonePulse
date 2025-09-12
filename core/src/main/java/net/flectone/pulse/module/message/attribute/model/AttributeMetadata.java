package net.flectone.pulse.module.message.attribute.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

@Getter
@SuperBuilder
public class AttributeMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @NonNull
    private final Attribute attribute;

    @NonNull
    private final MinecraftTranslationKey translationKey;

}