package net.flectone.pulse.model.event.module;

import lombok.With;
import net.flectone.pulse.module.ModuleSimple;

/**
 * Fired before a module is switched on. Cancelling it keeps the module off.
 *
 * @param cancelled whether a listener vetoed the change
 * @param module the module being enabled
 * @author TheFaser
 */
@With
public record ModuleEnableEvent(
        boolean cancelled,
        ModuleSimple module
) implements ModuleEvent {

    /**
     * Creates an event that has not been cancelled.
     *
     * @param module the module being enabled
     */
    public ModuleEnableEvent(ModuleSimple module) {
        this(false, module);
    }

}
