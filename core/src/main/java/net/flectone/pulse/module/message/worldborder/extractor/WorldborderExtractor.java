package net.flectone.pulse.module.message.worldborder.extractor;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.message.worldborder.model.Worldborder;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WorldborderExtractor extends Extractor {

    private final PacketProvider packetProvider;

    // Set the world border damage to %s per block each second
    // Set the world border damage buffer to %s block(s)
    // The world border is currently %s block(s) wide
    // Set the world border to %s block(s) wide
    // Set the world border warning distance to %s block(s)
    // Set the world border warning time to %s second(s)
    // World border is currently %s blocks wide
    public Optional<Worldborder> extractValue(TranslatableComponent translatableComponent) {
        Optional<String> value = extractTextContent(translatableComponent, 0);
        if (value.isEmpty()) return Optional.empty();

        Worldborder worldborder = new Worldborder(value.get(), null);
        return Optional.of(worldborder);
    }

    // Set the center of the world border to %s, %s
    // Growing the world border to %s blocks wide over %s seconds
    // Shrinking the world border to %s block(s) wide over %s second(s)
    public Optional<Worldborder> extractSecondValue(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        Optional<String> value = extractTextContent(translatableComponent, 0);
        if (value.isEmpty()) return Optional.empty();

        if (packetProvider.getServerVersion().isOlderThan(ServerVersion.V_1_16)) {
            // Shrinking world border to %s blocks wide (down from %s blocks) over %s seconds
            // Growing world border to %s blocks wide (up from %s blocks) over %s seconds
            if (translationKey == MinecraftTranslationKey.COMMANDS_WORLDBORDER_SETSLOWLY_GROW_SUCCESS
                    || translationKey == MinecraftTranslationKey.COMMANDS_WORLDBORDER_SETSLOWLY_SHRINK_SUCCESS) {
                // skip blocks parameter
                Optional<String> secondValue = extractTextContent(translatableComponent, 2);
                if (secondValue.isEmpty()) return Optional.empty();

                Worldborder worldborder = new Worldborder(value.get(), secondValue.get());
                return Optional.of(worldborder);
            }
        }

        Optional<String> secondValue = extractTextContent(translatableComponent, 1);
        if (secondValue.isEmpty()) return Optional.empty();

        Worldborder worldborder = new Worldborder(value.get(), secondValue.get());
        return Optional.of(worldborder);
    }

}