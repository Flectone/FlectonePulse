package net.flectone.pulse.model.event.message;

import lombok.Getter;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TranslatableComponent;

@Getter
public class TranslatableMessageReceiveEvent extends Event {

    private final FPlayer fPlayer;
    private final MinecraftTranslationKey key;
    private final TranslatableComponent component;

    public TranslatableMessageReceiveEvent(FPlayer fPlayer,
                                           MinecraftTranslationKey key,
                                           TranslatableComponent component) {
        this.fPlayer = fPlayer;
        this.key = key;
        this.component = component;
    }

}
