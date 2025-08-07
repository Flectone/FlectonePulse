package net.flectone.pulse.module.message.deop.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class DeopExtractor {

    @Inject
    public DeopExtractor() {
    }

    public Optional<String> extract(MessageReceiveEvent event) {
        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        if (translatableComponent.args().isEmpty()) return Optional.empty();
        if (!(translatableComponent.args().get(0) instanceof TextComponent targetComponent)) return Optional.empty();

        return Optional.of(targetComponent.content());
    }
}
