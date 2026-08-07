package net.flectone.pulse.model.event.module;

import lombok.With;
import net.flectone.pulse.module.ModuleSimple;

/**
 * Fired before a module is switched off. Cancelling it keeps the module running.
 *
 * @param cancelled whether a listener vetoed the change
 * @param module the module being disabled
 * @author TheFaser
 */
@With
public record ModuleDisableEvent(
        boolean cancelled,
        ModuleSimple module
) implements ModuleEvent {

    /**
     * Creates an event that has not been cancelled.
     *
     * @param module the module being disabled
     */
    public ModuleDisableEvent(ModuleSimple module) {
        this(false, module);
    }

}