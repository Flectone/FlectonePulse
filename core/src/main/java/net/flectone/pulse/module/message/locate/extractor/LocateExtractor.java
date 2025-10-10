package net.flectone.pulse.module.message.locate.extractor;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.message.locate.model.Locate;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LocateExtractor extends Extractor {

    private final PacketProvider packetProvider;

    public Optional<Locate> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        // Located %s at %s (y?) %s
        if (translationKey == MinecraftTranslationKey.COMMANDS_LOCATE_SUCCESS
                && packetProvider.getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_12_2)) {
            Optional<String> value = extractTextContent(translatableComponent, 0);
            if (value.isEmpty()) return Optional.empty();

            Optional<String> x = extractTextContent(translatableComponent, 1);
            if (x.isEmpty()) return Optional.empty();

            Optional<String> z = extractTextContent(translatableComponent, 2);
            if (z.isEmpty()) return Optional.empty();

            Locate locate = new Locate(value.get(), x.get(), "~", z.get(), "");
            return Optional.of(locate);
        }

        // The nearest %s is at %s (%s blocks away)
        Optional<String> value = extractTextContent(translatableComponent, 0);
        if (value.isEmpty()) return Optional.empty();

        Optional<Component> chatComponent = getValueComponent(translatableComponent, 1);
        if (chatComponent.isEmpty()) return Optional.empty();
        if (!(chatComponent.get() instanceof TranslatableComponent translatableChatComponent)) return Optional.empty();

        Optional<String> x = extractTextContent(translatableChatComponent, 0);
        if (x.isEmpty()) return Optional.empty();

        Optional<String> y = extractTextContent(translatableChatComponent, 1);
        if (y.isEmpty()) return Optional.empty();

        Optional<String> z = extractTextContent(translatableChatComponent, 2);
        if (z.isEmpty()) return Optional.empty();

        Optional<String> blocks = extractTextContent(translatableComponent, 2);
        if (blocks.isEmpty()) return Optional.empty();

        Locate locate = new Locate(value.get(), x.get(), y.get(), z.get(), blocks.get());
        return Optional.of(locate);
    }

}