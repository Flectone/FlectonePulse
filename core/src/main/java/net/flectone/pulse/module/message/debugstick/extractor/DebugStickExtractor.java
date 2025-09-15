package net.flectone.pulse.module.message.debugstick.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.debugstick.model.DebugStick;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class DebugStickExtractor extends Extractor {

    @Inject
    public DebugStickExtractor() {
    }

    public Optional<DebugStick> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        Optional<String> property = extractTextContent(translatableComponent, 0);
        if (property.isEmpty()) return Optional.empty();

        // %s has no properties
        Optional<String> value = Optional.empty();

        // selected \"%s\" (%s)
        // \"%s\" to %s
        if (translationKey != MinecraftTranslationKey.ITEM_MINECRAFT_DEBUG_STICK_EMPTY) {
            value = extractTextContent(translatableComponent, 1);
        }

        DebugStick debugStick = DebugStick.builder()
                .property(property.get())
                .value(value.orElse(null))
                .build();

        return Optional.of(debugStick);
    }

}