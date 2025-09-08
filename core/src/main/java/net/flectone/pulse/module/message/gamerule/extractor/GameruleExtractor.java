package net.flectone.pulse.module.message.gamerule.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.gamerule.model.Gamerule;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class GameruleExtractor extends Extractor {

    @Inject
    public GameruleExtractor() {
    }

    public Optional<Gamerule> extract(TranslatableComponent translatableComponent) {
        if (translatableComponent.arguments().size() < 2) return Optional.empty();
        if (!(translatableComponent.arguments().get(0).asComponent() instanceof TextComponent nameComponent)) return Optional.empty();
        if (!(translatableComponent.arguments().get(1).asComponent() instanceof TextComponent valueComponent)) return Optional.empty();

        Gamerule gamerule = new Gamerule(nameComponent.content(), valueComponent.content());
        return Optional.of(gamerule);
    }

}
