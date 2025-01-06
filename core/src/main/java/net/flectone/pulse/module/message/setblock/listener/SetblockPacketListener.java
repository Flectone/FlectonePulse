package net.flectone.pulse.module.message.setblock.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.setblock.SetblockModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.List;

@Singleton
public class SetblockPacketListener extends AbstractPacketListener {

    private final SetblockModule setblockModule;

    @Inject
    public SetblockPacketListener(SetblockModule setblockModule) {
        this.setblockModule = setblockModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;

        TranslatableComponent translatableComponent = getTranslatableComponent(event);
        if (translatableComponent == null) return;

        String key = translatableComponent.key();
        if (cancelMessageNotDelivered(event, key)) return;
        if (!key.startsWith("commands.setblock.success")) return;
        List<Component> translationArguments = translatableComponent.args();
        if (translationArguments.size() < 3) return;
        if (!setblockModule.isEnable()) return;

        if (!(translationArguments.get(0) instanceof TextComponent xComponent)) return;
        String x = xComponent.content();

        if (!(translationArguments.get(1) instanceof TextComponent yComponent)) return;
        String y = yComponent.content();

        if (!(translationArguments.get(2) instanceof TextComponent zComponent)) return;
        String z = zComponent.content();

        event.setCancelled(true);

        setblockModule.send(event.getUser().getUUID(), x, y, z);
    }
}
