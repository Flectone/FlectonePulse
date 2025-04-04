package net.flectone.pulse.module.message.rightclick;

import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.module.AbstractModuleMessage;

import java.util.function.Function;

public abstract class RightclickModule extends AbstractModuleMessage<Localization.Message.Rightclick> {

    public RightclickModule(Function<Localization, Localization.Message.Rightclick> messageFunction) {
        super(messageFunction);
    }

}
