package net.flectone.pulse.model.event.module;

import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.module.ModuleSimple;

public interface ModuleEvent extends Event {

    ModuleSimple module();

    ModuleEvent withModule(ModuleSimple module);

}
