package net.flectone.pulse.module.message.teleport.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.teleport.model.TeleportEntity;
import net.flectone.pulse.module.message.teleport.model.TeleportLocation;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;

import java.util.List;
import java.util.Optional;

@Singleton
public class TeleportExtractor extends Extractor {

    @Inject
    public TeleportExtractor() {
    }

    public Optional<TeleportEntity> extractEntity(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        if (translatableComponent.arguments().size() < 2) return Optional.empty();

        Component destinationComponent = translatableComponent.arguments().get(1).asComponent();
        Optional<FEntity> optionalDestination = extractFEntity(destinationComponent);
        if (optionalDestination.isEmpty()) return Optional.empty();

        Component component = translatableComponent.arguments().getFirst().asComponent();

        if (translationKey == MinecraftTranslationKey.COMMANDS_TELEPORT_SUCCESS_ENTITY_MULTIPLE) {
            if (!(component instanceof TextComponent countComponent)) return Optional.empty();

            String count = countComponent.content();
            TeleportEntity teleportEntity = new TeleportEntity(null, count, optionalDestination.get());
            return Optional.of(teleportEntity);
        }

        Optional<FEntity> optionalTarget = extractFEntity(component);
        if (optionalTarget.isEmpty()) return Optional.empty();

        TeleportEntity teleportEntity = new TeleportEntity(optionalTarget.get(), null, optionalDestination.get());
        return Optional.of(teleportEntity);
    }

    public Optional<TeleportLocation> extractLocation(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        List<TranslationArgument> translationArguments = translatableComponent.arguments();
        if (translationArguments.size() < 4) return Optional.empty();
        if (!(translationArguments.get(1).asComponent() instanceof TextComponent xComponent)) return Optional.empty();
        if (!(translationArguments.get(2).asComponent() instanceof TextComponent yComponent)) return Optional.empty();
        if (!(translationArguments.get(3).asComponent() instanceof TextComponent zComponent)) return Optional.empty();

        String x = xComponent.content();
        String y = yComponent.content();
        String z = zComponent.content();

        Component component = translatableComponent.arguments().getFirst().asComponent();

        if (translationKey == MinecraftTranslationKey.COMMANDS_TELEPORT_SUCCESS_LOCATION_MULTIPLE) {
            if (!(component instanceof TextComponent countComponent)) return Optional.empty();

            String count = countComponent.content();
            TeleportLocation teleportLocation = new TeleportLocation(null, count, x, y, z);
            return Optional.of(teleportLocation);
        }

        Optional<FEntity> optionalTarget = extractFEntity(component);
        if (optionalTarget.isEmpty()) return Optional.empty();

        TeleportLocation teleportLocation = new TeleportLocation(optionalTarget.get(), null, x, y, z);
        return Optional.of(teleportLocation);
    }
}
