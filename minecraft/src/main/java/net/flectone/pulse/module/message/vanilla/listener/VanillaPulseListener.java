package net.flectone.pulse.module.message.vanilla.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.vanilla.MinecraftVanillaModule;
import net.flectone.pulse.module.message.vanilla.extractor.Extractor;
import net.flectone.pulse.module.message.vanilla.model.ParsedComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VanillaPulseListener implements PulseListener {

    private final MinecraftVanillaModule vanillaModule;
    private final Extractor extractor;

    @Pulse
    public Event onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        if (translatableComponent == null) return event;

        Optional<ParsedComponent> parsedComponent = extractor.extract(translatableComponent);
        if (parsedComponent.isEmpty()) return event;

        vanillaModule.send(event.player(), parsedComponent.get());

        return event.withCancelled(true);
    }

}
