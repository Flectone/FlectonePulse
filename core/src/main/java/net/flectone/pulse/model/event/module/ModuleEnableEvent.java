package net.flectone.pulse.model.event.module;

import lombok.With;
import net.flectone.pulse.module.AbstractModule;

@With
public record ModuleEnableEvent(
        boolean cancelled,
        AbstractModule module
) implements ModuleEvent {

    public ModuleEnableEvent(AbstractModule module) {
        this(false, module);
    }

}
