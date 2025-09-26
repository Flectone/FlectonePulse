package net.flectone.pulse.module.message.effect;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.effect.listener.EffectPulseListener;
import net.flectone.pulse.module.message.effect.model.Effect;
import net.flectone.pulse.module.message.effect.model.EffectMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class EffectModule extends AbstractModuleLocalization<Localization.Message.Effect> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public EffectModule(FileResolver fileResolver,
                        ListenerRegistry listenerRegistry) {
        super(MessageType.EFFECT);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(EffectPulseListener.class);
    }

    @Override
    public Message.Effect config() {
        return fileResolver.getMessage().getEffect();
    }

    @Override
    public Permission.Message.Effect permission() {
        return fileResolver.getPermission().getMessage().getEffect();
    }

    @Override
    public Localization.Message.Effect localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getEffect();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Effect effect) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(EffectMetadata.<Localization.Message.Effect>builder()
                .sender(fPlayer)
                .format(string -> StringUtils.replaceEach(
                        switch (translationKey) {
                            case COMMANDS_EFFECT_CLEAR_EVERYTHING_SUCCESS_SINGLE -> string.getClear().getEverything().getSingle();
                            case COMMANDS_EFFECT_CLEAR_EVERYTHING_SUCCESS_MULTIPLE -> string.getClear().getEverything().getMultiple();
                            case COMMANDS_EFFECT_CLEAR_SPECIFIC_SUCCESS_SINGLE -> string.getClear().getSpecific().getSingle();
                            case COMMANDS_EFFECT_CLEAR_SPECIFIC_SUCCESS_MULTIPLE -> string.getClear().getSpecific().getMultiple();
                            case COMMANDS_EFFECT_GIVE_SUCCESS_SINGLE -> string.getGive().getSingle();
                            case COMMANDS_EFFECT_GIVE_SUCCESS_MULTIPLE -> string.getGive().getMultiple();
                            default -> "";
                        },
                        new String[]{"<effect>", "<players>"},
                        new String[]{StringUtils.defaultString(effect.getName()), StringUtils.defaultString(effect.getPlayers())}
                ))
                .range(config().getRange())
                .destination(config().getDestination())
                .sound(getModuleSound())
                .effect(effect)
                .translationKey(translationKey)
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, effect.getTarget())})
                .build()
        );
    }
}