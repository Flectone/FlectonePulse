package net.flectone.pulse.module.message.mark;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.module.AbstractModuleMessage;

import java.util.function.Function;

public abstract class MarkModule extends AbstractModuleMessage<Localization.Message> {

    public MarkModule(Function<Localization, Localization.Message> messageFunction) {
        super(messageFunction);
    }

}
