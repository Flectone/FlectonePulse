package net.flectone.pulse.model.event.module;

import net.flectone.pulse.module.AbstractModule;

public class ModuleEnableEvent extends ModuleEvent {

    public ModuleEnableEvent(AbstractModule module) {
        super(module);
    }

}
