package net.flectone.pulse.module.message.kill.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.kill.model.Kill;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class KillExtractor extends Extractor {

    @Inject
    public KillExtractor() {
    }

    public Optional<Kill> extractMultipleKill(MessageReceiveEvent event) {
        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        if (!(translatableComponent.arguments().getFirst().asComponent() instanceof TextComponent firstArgument)) return Optional.empty();

        String value = firstArgument.content();
        Kill kill = new Kill(value, null);
        return Optional.of(kill);
    }

    public Optional<Kill> extractSingleKill(MessageReceiveEvent event) {
        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        if (translatableComponent.arguments().isEmpty()) return Optional.empty();

        Component firstArgument = translatableComponent.arguments().getFirst().asComponent();
        Optional<FEntity> optionalFEntity = extractFEntity(firstArgument);
        if (optionalFEntity.isEmpty()) return Optional.empty();

        Kill kill = new Kill("", optionalFEntity.get());
        return Optional.of(kill);
    }

}
