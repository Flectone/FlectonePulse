package net.flectone.pulse.module.message.effect;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.effect.listener.EffectPulseListener;
import net.flectone.pulse.module.message.effect.model.Effect;
import net.flectone.pulse.module.message.effect.model.EffectMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class EffectModule extends AbstractModuleLocalization<Localization.Message.Effect> {

    private final Message.Effect message;
    private final Permission.Message.Effect permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public EffectModule(FileResolver fileResolver,
                        ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getEffect(), MessageType.EFFECT);

        this.message = fileResolver.getMessage().getEffect();
        this.permission = fileResolver.getPermission().getMessage().getEffect();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(EffectPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Effect effect) {
        if (isModuleDisabledFor(fPlayer)) return;
        if (effect.isIncorrect()) return;

        sendMessage(EffectMetadata.<Localization.Message.Effect>builder()
                .sender(effect.target() == null ? fPlayer : effect.target())
                .filterPlayer(fPlayer)
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
                        new String[]{"<effect>", "<count>"},
                        new String[]{StringUtils.defaultString(effect.name()), StringUtils.defaultString(effect.count())}
                ))
                .destination(message.getDestination())
                .sound(getModuleSound())
                .effect(effect)
                .translationKey(translationKey)
                .build()
        );
    }

}