package net.flectone.pulse.module.message.execute.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.jetbrains.annotations.Nullable;

@Getter
@SuperBuilder
public class ExecuteMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @Nullable
    private final String count;

    @NonNull
    private final MinecraftTranslationKey translationKey;

}