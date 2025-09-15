package net.flectone.pulse.module.message.locate.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.locate.model.Locate;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class LocateExtractor extends Extractor {

    @Inject
    public LocateExtractor() {
    }

    // The nearest %s is at %s (%s blocks away)
    public Optional<Locate> extract(TranslatableComponent translatableComponent) {
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