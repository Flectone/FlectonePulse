package net.flectone.pulse.module.message.enchant.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.enchant.EnchantModule;
import net.flectone.pulse.module.message.enchant.extractor.EnchantExtractor;
import net.flectone.pulse.module.message.enchant.model.Enchant;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EnchantPulseListener implements PulseListener {

    private final EnchantModule enchantModule;
    private final EnchantExtractor enchantExtractor;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (!translationKey.startsWith("commands.enchant.success")) return;

        Optional<Enchant> enchant = enchantExtractor.extract(translationKey, event.getTranslatableComponent());
        if (enchant.isEmpty()) return;

        event.setCancelled(true);
        enchantModule.send(event.getFPlayer(), event.getTranslationKey(), enchant.get());
    }

}
