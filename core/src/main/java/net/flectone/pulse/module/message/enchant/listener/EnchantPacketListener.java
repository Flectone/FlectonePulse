package net.flectone.pulse.module.message.enchant.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.enchant.EnchantModule;
import net.flectone.pulse.util.MinecraftTranslationKeys;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class EnchantPacketListener extends AbstractPacketListener {

    private final EnchantModule enchantModule;

    @Inject
    public EnchantPacketListener(EnchantModule enchantModule) {
        this.enchantModule = enchantModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;

        TranslatableComponent translatableComponent = getTranslatableComponent(event);
        if (translatableComponent == null) return;

        MinecraftTranslationKeys key = MinecraftTranslationKeys.fromString(translatableComponent);
        if (cancelMessageNotDelivered(event, key)) return;
        if (!key.startsWith("commands.enchant.success")) return;
        if (translatableComponent.args().size() < 2) return;
        if (!enchantModule.isEnable()) return;

        if (!(translatableComponent.args().get(0) instanceof TranslatableComponent enchantComponent)) return;

        String enchantKey = enchantComponent.key();

        if (enchantComponent.children().size() < 2) return;
        if (!(enchantComponent.children().get(1) instanceof TranslatableComponent levelComponent)) return;

        String levelKey = levelComponent.key();

        if (!(translatableComponent.args().get(1) instanceof TextComponent targetComponent)) return;
        String value = targetComponent.content();

        event.setCancelled(true);

        enchantModule.send(event.getUser().getUUID(), key, enchantKey, levelKey, value);
    }
}
