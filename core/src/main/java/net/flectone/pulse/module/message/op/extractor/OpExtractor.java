package net.flectone.pulse.module.message.op.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class OpExtractor {

    @Inject
    public OpExtractor() {
    }

    public Optional<String> extract(TranslatableMessageReceiveEvent event) {
        TranslatableComponent translatableComponent = event.getComponent();
        if (translatableComponent.args().isEmpty()) return Optional.empty();
        if (!(translatableComponent.args().get(0) instanceof TextComponent targetComponent)) return Optional.empty();

        return Optional.of(targetComponent.content());
    }

}
