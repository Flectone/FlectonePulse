package net.flectone.pulse.module.message.recipe.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.recipe.RecipeModule;
import net.flectone.pulse.module.message.recipe.extractor.RecipeExtractor;
import net.flectone.pulse.module.message.recipe.model.Recipe;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class RecipePulseListener implements PulseListener {

    private final RecipeModule recipeModule;
    private final RecipeExtractor recipeExtractor;

    @Inject
    public RecipePulseListener(RecipeModule recipeModule,
                               RecipeExtractor recipeExtractor) {
        this.recipeModule = recipeModule;
        this.recipeExtractor = recipeExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        switch (translationKey) {
            case COMMANDS_RECIPE_GIVE_SUCCESS_MULTIPLE, COMMANDS_RECIPE_GIVE_SUCCESS_SINGLE,
                 COMMANDS_RECIPE_TAKE_SUCCESS_MULTIPLE, COMMANDS_RECIPE_TAKE_SUCCESS_SINGLE -> {
                Optional<Recipe> recipe = recipeExtractor.extract(translationKey, event.getTranslatableComponent());
                if (recipe.isEmpty()) return;

                event.setCancelled(true);
                recipeModule.send(event.getFPlayer(), translationKey, recipe.get());
            }
        }
    }

}
