package net.flectone.pulse.module.message.summon.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class SummonExtractor extends Extractor {

    @Inject
    public SummonExtractor() {
    }

    public Optional<FEntity> extract(TranslatableComponent translatableComponent) {
        if (translatableComponent.arguments().isEmpty()) return Optional.empty();

        Component component = translatableComponent.arguments().getFirst().asComponent();
        return extractFEntity(component);
    }

}
