package net.flectone.pulse.module.message.setblock.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.setblock.model.Setblock;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.List;
import java.util.Optional;

@Singleton
public class SetblockExtractor extends Extractor {

    @Inject
    public SetblockExtractor() {
    }

    public Optional<Setblock> extract(MessageReceiveEvent event) {
        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        List<Component> translationArguments = translatableComponent.args();

        String x = "";
        String y = "";
        String z = "";
        if (translationArguments.size() > 2) {
            if (!(translationArguments.get(0) instanceof TextComponent xComponent)) return Optional.empty();
            if (!(translationArguments.get(1) instanceof TextComponent yComponent)) return Optional.empty();
            if (!(translationArguments.get(2) instanceof TextComponent zComponent)) return Optional.empty();

            x = xComponent.content();
            y = yComponent.content();
            z = zComponent.content();
        }

        Setblock setblock = new Setblock(x, y, z);
        return Optional.of(setblock);
    }

}
