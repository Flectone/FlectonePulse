package net.flectone.pulse.module.message.summon.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.summon.SummonModule;
import net.flectone.pulse.module.message.summon.extractor.SummonExtractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SummonPulseListener implements PulseListener {

    private final SummonExtractor summonExtractor;
    private final SummonModule summonModule;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (translationKey != MinecraftTranslationKey.COMMANDS_SUMMON_SUCCESS) return;

        Optional<FEntity> target = summonExtractor.extract(event.getTranslatableComponent());

        event.setCancelled(true);
        summonModule.send(event.getFPlayer(), translationKey, target.orElse(null));
    }
}
