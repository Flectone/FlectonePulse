package net.flectone.pulse.module.message.recipe;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.recipe.listener.RecipePulseListener;
import net.flectone.pulse.module.message.recipe.model.Recipe;
import net.flectone.pulse.module.message.recipe.model.RecipeMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RecipeModule extends AbstractModuleLocalization<Localization.Message.Recipe> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(RecipePulseListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.RECIPE;
    }

    @Override
    public Message.Recipe config() {
        return fileResolver.getMessage().getRecipe();
    }

    @Override
    public Permission.Message.Recipe permission() {
        return fileResolver.getPermission().getMessage().getRecipe();
    }

    @Override
    public Localization.Message.Recipe localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getRecipe();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Recipe recipe) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(RecipeMetadata.<Localization.Message.Recipe>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(localization -> StringUtils.replaceEach(
                        switch (translationKey) {
                            case COMMANDS_RECIPE_GIVE_SUCCESS_MULTIPLE -> localization.getGive().getMultiple();
                            case COMMANDS_RECIPE_GIVE_SUCCESS_SINGLE -> localization.getGive().getSingle();
                            case COMMANDS_RECIPE_TAKE_SUCCESS_MULTIPLE -> localization.getTake().getMultiple();
                            case COMMANDS_RECIPE_TAKE_SUCCESS_SINGLE -> localization.getTake().getSingle();
                            default -> "";
                        },
                        new String[]{"<recipes>", "<players>"},
                        new String[]{recipe.getRecipes(), StringUtils.defaultString(recipe.getPlayers())}
                ))
                .recipe(recipe)
                .translationKey(translationKey)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, recipe.getTarget())})
                .build()
        );
    }
}
