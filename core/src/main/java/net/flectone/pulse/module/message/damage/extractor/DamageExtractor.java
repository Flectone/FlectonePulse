package net.flectone.pulse.module.message.damage.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.damage.model.Damage;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class DamageExtractor extends Extractor {

    @Inject
    public DamageExtractor() {
    }

    public Optional<Damage> extract(TranslatableComponent translatableComponent) {
        if (translatableComponent.arguments().size() < 2) return Optional.empty();
        if (!(translatableComponent.arguments().get(0).asComponent() instanceof TextComponent amountComponent)) return Optional.empty();

        Optional<FEntity> optionalFEntity = extractFEntity(translatableComponent.arguments().get(1).asComponent());
        if (optionalFEntity.isEmpty()) return Optional.empty();

        String amount = amountComponent.content();
        Damage damage = new Damage(amount, optionalFEntity.get());
        return Optional.of(damage);
    }

}
