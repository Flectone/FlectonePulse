package net.flectone.pulse.model.event.module;

import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.module.ModuleSimple;

/**
 * Fired when a module is switched on or off.
 * @author TheFaser
 */
public interface ModuleEvent extends Event {

    /**
     * The module the event is about.
     *
     * @return the module
     */
    ModuleSimple module();

    /**
     * Returns a copy of this event for a different module.
     *
     * @param module the new module
     * @return the copy
     */
    ModuleEvent withModule(ModuleSimple module);

}
