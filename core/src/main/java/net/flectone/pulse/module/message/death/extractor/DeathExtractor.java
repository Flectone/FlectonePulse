package net.flectone.pulse.module.message.death.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.death.model.Death;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class DeathExtractor extends Extractor {

    @Inject
    public DeathExtractor() {
    }

    // example
    // %1$s was shot by %2$s using %3$s
    public Optional<Death> extract(TranslatableComponent translatableComponent) {
        Optional<FEntity> target = extractFEntity(translatableComponent, 0);
        if (target.isEmpty()) return Optional.empty();

        Optional<FEntity> killer = extractFEntity(translatableComponent, 1);
        Optional<Component> killerItem = getValueComponent(translatableComponent, 2);

        Death death = Death.builder()
                .target(target.get())
                .killer(killer.orElse(null))
                .killerItem(killerItem.orElse(null))
                .build();

        return Optional.of(death);
    }

}
