package net.flectone.pulse.module.message.give.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.give.model.Give;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Singleton
public class GiveExtractor extends Extractor {

    @Inject
    public GiveExtractor() {
    }

    // Gave %s %s to %s players
    // Gave %s %s to %s
    public Optional<Give> extract(TranslatableComponent translatableComponent) {
        Optional<String> items = extractTextContent(translatableComponent, 0);
        if (items.isEmpty()) return Optional.empty();

        Optional<Component> item = getValueComponent(translatableComponent, 1);
        if (item.isEmpty()) return Optional.empty();

        // idk why but "commands.give.success.multiple" not called
        Optional<String> players = extractTextContent(translatableComponent, 2);
        if (players.isPresent() && StringUtils.isNumeric(players.get())) {
            Give give = Give.builder()
                    .items(items.get())
                    .item(item.get())
                    .players(players.get())
                    .build();

            return Optional.of(give);
        }

        Optional<FEntity> target = extractFEntity(translatableComponent, 2);
        if (target.isEmpty()) return Optional.empty();

        Give give = Give.builder()
                .items(items.get())
                .item(item.get())
                .target(target.get())
                .build();

        return Optional.of(give);
    }
}