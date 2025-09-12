package net.flectone.pulse.module.message.debugstick.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.debugstick.model.DebugStick;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;

import java.util.List;
import java.util.Optional;

@Singleton
public class DebugStickExtractor extends Extractor {

    @Inject
    public DebugStickExtractor() {
    }

    public Optional<DebugStick> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        List<TranslationArgument> translationArguments = translatableComponent.arguments();
        if (translationArguments.isEmpty()) return Optional.empty();
        if (!(translationArguments.get(0).asComponent() instanceof TextComponent nameComponent)) return Optional.empty();

        String value = null;
        if (translationKey != MinecraftTranslationKey.ITEM_MINECRAFT_DEBUG_STICK_EMPTY) {
            if (translationArguments.size() < 2) return Optional.empty();
            if (!(translationArguments.get(1).asComponent() instanceof TextComponent valueComponent)) return Optional.empty();

            value = valueComponent.content();
        }

        String name = nameComponent.content();
        DebugStick debugStick = new DebugStick(name, value);
        return Optional.of(debugStick);
    }

}