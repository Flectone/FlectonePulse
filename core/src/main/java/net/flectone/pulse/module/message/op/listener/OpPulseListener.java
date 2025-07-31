package net.flectone.pulse.module.message.op.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.constant.MinecraftTranslationKey;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.module.message.op.OpModule;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class OpPulseListener implements PulseListener {

    private final OpModule opModule;

    @Inject
    public OpPulseListener(OpModule opModule) {
        this.opModule = opModule;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(TranslatableMessageReceiveEvent event) {
        if (event.getKey() != MinecraftTranslationKey.COMMANDS_OP_SUCCESS) return;

        TranslatableComponent translatableComponent = event.getComponent();
        if (translatableComponent.args().isEmpty()) return;
        if (!(translatableComponent.args().get(0) instanceof TextComponent targetComponent)) return;

        event.cancelPacket();
        opModule.send(event.getFPlayer(), targetComponent.content());
    }

}
