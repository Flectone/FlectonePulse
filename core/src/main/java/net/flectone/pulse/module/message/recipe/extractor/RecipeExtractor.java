package net.flectone.pulse.module.message.recipe.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.recipe.model.Recipe;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RecipeExtractor extends Extractor {

    // Displaying particle %s
    public Optional<Recipe> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        return switch (translationKey) {
            case COMMANDS_RECIPE_GIVE_SUCCESS_MULTIPLE, COMMANDS_RECIPE_TAKE_SUCCESS_MULTIPLE -> {
                Optional<String> recipes = extractTextContent(translatableComponent, 0);
                if (recipes.isEmpty()) yield Optional.empty();

                Optional<String> players = extractTextContent(translatableComponent, 1);
                if (players.isEmpty()) yield Optional.empty();

                Recipe recipe = Recipe.builder()
                        .recipes(recipes.get())
                        .players(players.get())
                        .build();

                yield Optional.of(recipe);
            }
            case COMMANDS_RECIPE_GIVE_SUCCESS_SINGLE, COMMANDS_RECIPE_TAKE_SUCCESS_SINGLE -> {
                Optional<String> recipes = extractTextContent(translatableComponent, 0);
                if (recipes.isEmpty()) yield Optional.empty();

                Optional<FEntity> target = extractFEntity(translatableComponent, 1);
                if (target.isEmpty()) yield Optional.empty();

                Recipe recipe = Recipe.builder()
                        .recipes(recipes.get())
                        .target(target.get())
                        .build();

                yield Optional.of(recipe);
            }
            default -> Optional.empty();
        };
    }

}
