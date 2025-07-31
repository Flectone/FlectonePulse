package net.flectone.pulse.module.message.clear.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.constant.MinecraftTranslationKey;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.module.message.clear.ClearModule;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class ClearPulseListener implements PulseListener {

    private final ClearModule clearModule;

    @Inject
    public ClearPulseListener(ClearModule clearModule) {
        this.clearModule = clearModule;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(TranslatableMessageReceiveEvent event) {
        if (!event.getKey().startsWith("commands.clear.success")) return;

        TranslatableComponent translatableComponent = event.getComponent();
        if (translatableComponent.args().size() < 2) return;
        if (!(translatableComponent.args().get(0) instanceof TextComponent firstArg)) return;
        if (!(translatableComponent.args().get(1) instanceof TextComponent secondArg)) return;

        event.cancelPacket();

        if (event.getKey() == MinecraftTranslationKey.COMMANDS_CLEAR_SUCCESS) {
            clearModule.send(event.getFPlayer(), event.getKey(), secondArg.content(), firstArg.content());
        } else {
            clearModule.send(event.getFPlayer(), event.getKey(), firstArg.content(), secondArg.content());
        }
    }

}
