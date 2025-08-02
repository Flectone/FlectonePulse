package net.flectone.pulse.module.message.deop.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.module.message.deop.DeopModule;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class DeopPulseListener implements PulseListener {

    private final DeopModule deopModule;

    @Inject
    public DeopPulseListener(DeopModule deopModule) {
        this.deopModule = deopModule;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(TranslatableMessageReceiveEvent event) {
        if (event.getKey() != MinecraftTranslationKey.COMMANDS_DEOP_SUCCESS) return;

        TranslatableComponent translatableComponent = event.getComponent();
        if (translatableComponent.args().isEmpty()) return;
        if (!(translatableComponent.args().get(0) instanceof TextComponent targetComponent)) return;

        event.cancelPacket();
        deopModule.send(event.getFPlayer(), targetComponent.content());
    }

}
