package net.flectone.pulse.model.event.module;

import lombok.With;
import net.flectone.pulse.module.ModuleSimple;

@With
public record ModuleEnableEvent(
        boolean cancelled,
        ModuleSimple module
) implements ModuleEvent {

    public ModuleEnableEvent(ModuleSimple module) {
        this(false, module);
    }

}
