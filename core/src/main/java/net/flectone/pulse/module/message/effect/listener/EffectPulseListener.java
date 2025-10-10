package net.flectone.pulse.module.message.effect.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.effect.EffectModule;
import net.flectone.pulse.module.message.effect.extractor.EffectExtractor;
import net.flectone.pulse.module.message.effect.model.Effect;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EffectPulseListener implements PulseListener {

    private final EffectModule effectModule;
    private final EffectExtractor effectExtractor;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        switch (translationKey) {
            case COMMANDS_EFFECT_CLEAR_EVERYTHING_SUCCESS_MULTIPLE, COMMANDS_EFFECT_CLEAR_EVERYTHING_SUCCESS_SINGLE,
                 COMMANDS_EFFECT_CLEAR_SPECIFIC_SUCCESS_MULTIPLE, COMMANDS_EFFECT_CLEAR_SPECIFIC_SUCCESS_SINGLE,
                 COMMANDS_EFFECT_GIVE_SUCCESS_MULTIPLE, COMMANDS_EFFECT_GIVE_SUCCESS_SINGLE,
                 COMMANDS_EFFECT_SUCCESS, COMMANDS_EFFECT_SUCCESS_REMOVED, COMMANDS_EFFECT_SUCCESS_REMOVED_ALL -> {
                Optional<Effect> effect = effectExtractor.extract(translationKey, event.getTranslatableComponent());
                if (effect.isEmpty()) return;

                event.setCancelled(true);
                effectModule.send(event.getFPlayer(), translationKey, effect.get());
            }
        }
    }

}