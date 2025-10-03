package net.flectone.pulse.module.message.give.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.give.model.Give;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Singleton
public class GiveExtractor extends Extractor {

    @Inject
    public GiveExtractor() {
    }

    public Optional<Give> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        return switch (translationKey) {
            // Given %s * %d to %s
            case COMMANDS_GIVE_SUCCESS -> {
                Optional<Component> item = getValueComponent(translatableComponent, 0);
                if (item.isEmpty()) yield Optional.empty();

                Optional<String> items = extractTextContent(translatableComponent, 1);
                if (items.isEmpty()) yield Optional.empty();

                Optional<FEntity> target = extractFEntity(translatableComponent, 2);
                if (target.isEmpty()) yield Optional.empty();

                Give give = Give.builder()
                        .items(items.get())
                        .item(item.get())
                        .target(target.get())
                        .build();

                yield Optional.of(give);
            }
            // Gave %s %s to %s players
            // Gave %s %s to %s
            case COMMANDS_GIVE_SUCCESS_MULTIPLE, COMMANDS_GIVE_SUCCESS_SINGLE -> {
                Optional<String> items = extractTextContent(translatableComponent, 0);
                if (items.isEmpty()) yield Optional.empty();

                Optional<Component> item = getValueComponent(translatableComponent, 1);
                if (item.isEmpty()) yield Optional.empty();

                // idk why but "commands.give.success.multiple" not called
                Optional<String> players = extractTextContent(translatableComponent, 2);
                if (players.isPresent() && StringUtils.isNumeric(players.get())) {
                    Give give = Give.builder()
                            .items(items.get())
                            .item(item.get())
                            .players(players.get())
                            .build();

                    yield Optional.of(give);
                }

                Optional<FEntity> target = extractFEntity(translatableComponent, 2);
                if (target.isEmpty()) yield Optional.empty();

                Give give = Give.builder()
                        .items(items.get())
                        .item(item.get())
                        .target(target.get())
                        .build();

                yield Optional.of(give);
            }

            default -> Optional.empty();
        };
    }
}