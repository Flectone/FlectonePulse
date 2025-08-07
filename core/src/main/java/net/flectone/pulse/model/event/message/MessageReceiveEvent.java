package net.flectone.pulse.model.event.message;

import lombok.Getter;
import net.flectone.pulse.exception.NotTranslatableException;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.incendo.cloud.type.tuple.Triplet;

@Getter
public class MessageReceiveEvent extends Event {

    private final FPlayer fPlayer;
    private final Component component;
    private final MinecraftTranslationKey translationKey;
    private final boolean overlay;

    public MessageReceiveEvent(FPlayer fPlayer, Component component, MinecraftTranslationKey translationKey, boolean overlay) {
        this.fPlayer = fPlayer;
        this.component = component;
        this.translationKey = translationKey;
        this.overlay = overlay;
    }

    public MessageReceiveEvent(FPlayer fPlayer, Triplet<Component, MinecraftTranslationKey, Boolean> triplet) {
        this(fPlayer, triplet.first(), triplet.second(), triplet.third());
    }

    public TranslatableComponent getTranslatableComponent() {
        if (translationKey == MinecraftTranslationKey.UNKNOWN) throw new NotTranslatableException();

        return (TranslatableComponent) component;
    }

}
