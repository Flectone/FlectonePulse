package net.flectone.pulse.module.message.enchant.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.module.message.enchant.EnchantModule;
import net.flectone.pulse.module.message.enchant.extractor.EnchantExtractor;
import net.flectone.pulse.module.message.enchant.model.Enchant;

import java.util.Optional;

@Singleton
public class EnchantPulseListener implements PulseListener {

    private final EnchantModule enchantModule;
    private final EnchantExtractor enchantExtractor;

    @Inject
    public EnchantPulseListener(EnchantModule enchantModule,
                                EnchantExtractor enchantExtractor) {
        this.enchantModule = enchantModule;
        this.enchantExtractor = enchantExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(TranslatableMessageReceiveEvent event) {
        Optional<Enchant> enchant = enchantExtractor.extractEnchant(event);
        if (enchant.isEmpty()) return;

        event.setCancelled(true);
        enchantModule.send(event.getFPlayer(), event.getKey(), enchant.get());
    }

}
