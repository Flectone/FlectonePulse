package net.flectone.pulse.module.message.enchant.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.module.message.enchant.EnchantModule;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class EnchantPulseListener implements PulseListener {

    private final EnchantModule enchantModule;

    @Inject
    public EnchantPulseListener(EnchantModule enchantModule) {
        this.enchantModule = enchantModule;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(TranslatableMessageReceiveEvent event) {
        if (!event.getKey().startsWith("commands.enchant.success")) return;
        if (event.getKey() == MinecraftTranslationKey.COMMANDS_ENCHANT_SUCCESS) {
            event.setCancelled(true);
            enchantModule.send(event.getFPlayer(), event.getKey(), "", "", "");
            return;
        }

        TranslatableComponent translatableComponent = event.getComponent();
        if (translatableComponent.args().size() < 2) return;
        if (!(translatableComponent.args().get(0) instanceof TranslatableComponent enchantComponent)) return;

        String enchantKey = enchantComponent.key();

        if (enchantComponent.children().size() < 2) return;
        if (!(enchantComponent.children().get(1) instanceof TranslatableComponent levelComponent)) return;

        String levelKey = levelComponent.key();

        if (!(translatableComponent.args().get(1) instanceof TextComponent targetComponent)) return;
        String value = targetComponent.content();

        event.setCancelled(true);
        enchantModule.send(event.getFPlayer(), event.getKey(), enchantKey, levelKey, value);
    }

}
