package net.flectone.pulse.module.message.particle.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.event.EventMetadata;

@Getter
@SuperBuilder
public class ParticleMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @NonNull
    private final String particle;

}