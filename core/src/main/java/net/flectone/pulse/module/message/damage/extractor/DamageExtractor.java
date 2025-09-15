package net.flectone.pulse.module.message.damage.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.damage.model.Damage;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class DamageExtractor extends Extractor {

    @Inject
    public DamageExtractor() {
    }

    // Applied %s damage to %s
    public Optional<Damage> extract(TranslatableComponent translatableComponent) {
        Optional<String> amount = extractTextContent(translatableComponent, 0);
        if (amount.isEmpty()) return Optional.empty();

        Optional<FEntity> target = extractFEntity(translatableComponent, 1);
        if (target.isEmpty()) return Optional.empty();

        Damage damage = new Damage(amount.get(), target.get());
        return Optional.of(damage);
    }

}
