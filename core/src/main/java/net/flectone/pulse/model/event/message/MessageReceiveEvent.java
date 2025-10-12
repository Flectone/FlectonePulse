package net.flectone.pulse.model.event.message;

import lombok.Getter;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.incendo.cloud.type.tuple.Pair;

@Getter
public class MessageReceiveEvent extends Event {

    private final FPlayer fPlayer;
    private final Component component;
    private final boolean overlay;

    public MessageReceiveEvent(FPlayer fPlayer, Component component, boolean overlay) {
        this.fPlayer = fPlayer;
        this.component = component;
        this.overlay = overlay;
    }

    public MessageReceiveEvent(FPlayer fPlayer, Pair<Component, Boolean> pair) {
        this(fPlayer, pair.first(), pair.second());
    }

    public TranslatableComponent getTranslatableComponent() {
        return component instanceof TranslatableComponent translatableComponent ? translatableComponent : null;
    }

}
