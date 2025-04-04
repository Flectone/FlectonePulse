package net.flectone.pulse.module.message.mark;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public abstract class MarkModule extends AbstractModuleMessage<Localization.Message> {

    public MarkModule(Function<Localization, Localization.Message> messageFunction) {
        super(messageFunction);
    }

    public abstract void mark(@NotNull FPlayer fPlayer, @NotNull NamedTextColor textColor);
}
