package net.flectone.pulse.model.event.module;

import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.module.AbstractModule;

public interface ModuleEvent extends Event {

    AbstractModule module();

    ModuleEvent withModule(AbstractModule module);

}
