package net.flectone.pulse.module.message.seed.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.module.message.seed.SeedModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class SeedPulseListener implements PulseListener {

    private final SeedModule seedModule;

    @Inject
    public SeedPulseListener(SeedModule seedModule) {
        this.seedModule = seedModule;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(TranslatableMessageReceiveEvent event) {
        if (event.getKey() != MinecraftTranslationKey.COMMANDS_SEED_SUCCESS) return;

        TranslatableComponent translatableComponent = event.getComponent();
        if (translatableComponent.args().isEmpty()) return;

        Component firstArg = translatableComponent.args().get(0);
        String seed = switch (firstArg) {
            // modern format with chat.square_brackets
            case TranslatableComponent chatComponent when chatComponent.key().equals("chat.square_brackets")
                    && !chatComponent.args().isEmpty()
                    && chatComponent.args().get(0) instanceof TextComponent seedComponent -> seedComponent.content();
            // legacy format with extra
            case TextComponent textComponent when textComponent.content().equals("[")
                    && !textComponent.children().isEmpty()
                    && textComponent.children().get(0) instanceof TextComponent seedComponent -> seedComponent.content();
            // legacy format
            case TextComponent textComponent when !textComponent.content().isEmpty() -> textComponent.content();
            default -> null;
        };

        if (seed == null) return;

        event.cancelPacket();
        seedModule.send(event.getFPlayer(), seed);
    }

}
