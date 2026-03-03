package net.flectone.pulse.model.event.module;

import lombok.With;
import net.flectone.pulse.module.ModuleSimple;

@With
public record ModuleDisableEvent(
        boolean cancelled,
        ModuleSimple module
) implements ModuleEvent {

    public ModuleDisableEvent(ModuleSimple module) {
        this(false, module);
    }

}