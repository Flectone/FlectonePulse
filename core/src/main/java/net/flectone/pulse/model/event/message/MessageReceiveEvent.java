package net.flectone.pulse.model.event.message;

import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

@With
public record MessageReceiveEvent(
        boolean cancelled,
        FPlayer player,
        Component component,
        boolean overlay
) implements Event {

    public MessageReceiveEvent(FPlayer fPlayer, Component component, boolean overlay) {
        this(false, fPlayer, component, overlay);
    }

    public TranslatableComponent getTranslatableComponent() {
        return component instanceof TranslatableComponent translatableComponent ? translatableComponent : null;
    }

}