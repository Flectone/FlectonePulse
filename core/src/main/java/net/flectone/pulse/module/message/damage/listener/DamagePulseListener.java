package net.flectone.pulse.module.message.damage.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.damage.DamageModule;
import net.flectone.pulse.module.message.damage.extractor.DamageExtractor;
import net.flectone.pulse.module.message.damage.model.Damage;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DamagePulseListener implements PulseListener {

    private final DamageExtractor summonExtractor;
    private final DamageModule damageModule;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (translationKey != MinecraftTranslationKey.COMMANDS_DAMAGE_SUCCESS) return;

        Optional<Damage> optionalDamage = summonExtractor.extract(event.getTranslatableComponent());
        if (optionalDamage.isEmpty()) return;

        event.setCancelled(true);
        damageModule.send(event.getFPlayer(), translationKey, optionalDamage.get());
    }
}
