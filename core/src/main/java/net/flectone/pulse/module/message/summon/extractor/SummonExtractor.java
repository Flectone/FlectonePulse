package net.flectone.pulse.module.message.summon.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;
import java.util.UUID;

@Singleton
public class SummonExtractor extends Extractor {

    @Inject
    public SummonExtractor() {
    }

    public Optional<FEntity> extract(TranslatableComponent translatableComponent) {
        if (translatableComponent.arguments().isEmpty()) return Optional.empty();

        Component component = translatableComponent.arguments().getFirst().asComponent();
        if (component instanceof TranslatableComponent entityComponent) {
            String stringEntityUUID = entityComponent.insertion();
            if (stringEntityUUID == null) return Optional.empty();

            String translationKey = entityComponent.key();
            Optional<UUID> optionalUUID = parseUUID(stringEntityUUID);
            return optionalUUID.map(uuid -> new FEntity(translationKey, uuid, translationKey));
        }

        if (component instanceof TextComponent entityComponent) {
            String stringEntityUUID = entityComponent.insertion();
            if (stringEntityUUID == null) return Optional.empty();

            Optional<UUID> optionalUUID = parseUUID(stringEntityUUID);
            return optionalUUID.map(uuid -> new FEntity(entityComponent.content(), uuid, ""));
        }

        return Optional.empty();
    }

}
