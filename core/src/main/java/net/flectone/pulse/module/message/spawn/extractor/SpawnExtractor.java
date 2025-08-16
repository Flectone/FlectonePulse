package net.flectone.pulse.module.message.spawn.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.module.message.spawn.model.Spawn;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.List;
import java.util.Optional;

@Singleton
public class SpawnExtractor extends Extractor {

    @Inject
    public SpawnExtractor() {
    }

    public Optional<Spawn> extract(MessageReceiveEvent event) {
        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        List<Component> translationArguments = translatableComponent.args();
        if (translationArguments.size() < 4) return Optional.empty();

        Component targetComponent;
        Component xComponent;
        Component yComponent;
        Component zComponent;
        String angle = "";
        String world = "";

        if (event.getTranslationKey() == MinecraftTranslationKey.COMMANDS_SPAWNPOINT_SUCCESS) {
            // legacy format, player first
            targetComponent = translationArguments.get(0);
            xComponent = translationArguments.get(1);
            yComponent = translationArguments.get(2);
            zComponent = translationArguments.get(3);
        } else {
            // coordinates first, player last
            xComponent = translationArguments.get(0);
            yComponent = translationArguments.get(1);
            zComponent = translationArguments.get(2);
            targetComponent = translationArguments.getLast();

            // check for optional angle and world
            if (translationArguments.size() >= 5 && translationArguments.get(3) instanceof TextComponent angleComponent) {
                angle = angleComponent.content();
            }

            if (translationArguments.size() >= 6 && translationArguments.get(4) instanceof TextComponent worldComponent) {
                world = worldComponent.content();
            }
        }

        if (!(xComponent instanceof TextComponent xComp)) return Optional.empty();
        if (!(yComponent instanceof TextComponent yComp)) return Optional.empty();
        if (!(zComponent instanceof TextComponent zComp)) return Optional.empty();
        if (!(targetComponent instanceof TextComponent tgtComp)) return Optional.empty();

        String x = xComp.content();
        String y = yComp.content();
        String z = zComp.content();
        String value = extractTarget(tgtComp);

        Spawn spawn = new Spawn(x, y, z, angle, world, value);
        return Optional.of(spawn);
    }

}
