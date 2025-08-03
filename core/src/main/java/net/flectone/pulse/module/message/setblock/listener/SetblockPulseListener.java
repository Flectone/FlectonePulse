package net.flectone.pulse.module.message.setblock.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.module.message.setblock.SetblockModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.List;

@Singleton
public class SetblockPulseListener implements PulseListener {

    private final SetblockModule setblockModule;

    @Inject
    public SetblockPulseListener(SetblockModule setblockModule) {
        this.setblockModule = setblockModule;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(TranslatableMessageReceiveEvent event) {
        if (event.getKey() != MinecraftTranslationKey.COMMANDS_SETBLOCK_SUCCESS) return;

        TranslatableComponent translatableComponent = event.getComponent();
        List<Component> translationArguments = translatableComponent.args();

        String x = "";
        String y = "";
        String z = "";
        if (translationArguments.size() > 2) {
            if (!(translationArguments.get(0) instanceof TextComponent xComponent)) return;
            if (!(translationArguments.get(1) instanceof TextComponent yComponent)) return;
            if (!(translationArguments.get(2) instanceof TextComponent zComponent)) return;

            x = xComponent.content();
            y = yComponent.content();
            z = zComponent.content();
        }

        event.setCancelled(true);

        setblockModule.send(event.getFPlayer(), x, y, z);
    }
}
