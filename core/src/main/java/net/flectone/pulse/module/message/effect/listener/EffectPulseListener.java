package net.flectone.pulse.module.message.effect.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.effect.EffectModule;
import net.flectone.pulse.module.message.effect.extractor.EffectExtractor;
import net.flectone.pulse.module.message.effect.model.Effect;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class EffectPulseListener implements PulseListener {

    private final EffectModule effectModule;
    private final EffectExtractor effectExtractor;

    @Inject
    public EffectPulseListener(EffectModule effectModule,
                               EffectExtractor effectExtractor) {
        this.effectModule = effectModule;
        this.effectExtractor = effectExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        Optional<Effect> effect = switch (translationKey) {
            case COMMANDS_EFFECT_CLEAR_EVERYTHING_SUCCESS_MULTIPLE, COMMANDS_EFFECT_CLEAR_EVERYTHING_SUCCESS_SINGLE ->
                    effectExtractor.extractTarget(translationKey, event.getTranslatableComponent());
            case COMMANDS_EFFECT_CLEAR_SPECIFIC_SUCCESS_MULTIPLE, COMMANDS_EFFECT_CLEAR_SPECIFIC_SUCCESS_SINGLE,
                 COMMANDS_EFFECT_GIVE_SUCCESS_MULTIPLE, COMMANDS_EFFECT_GIVE_SUCCESS_SINGLE ->
                    effectExtractor.extractNameAndTarget(translationKey, event.getTranslatableComponent());
            default -> Optional.empty();
        };

        if (effect.isEmpty()) return;

        event.setCancelled(true);
        effectModule.send(event.getFPlayer(), translationKey, effect.get());
    }

}