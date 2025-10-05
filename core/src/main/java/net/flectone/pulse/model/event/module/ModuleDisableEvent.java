package net.flectone.pulse.model.event.module;

import net.flectone.pulse.module.AbstractModule;

public class ModuleDisableEvent extends ModuleEvent {

    public ModuleDisableEvent(AbstractModule module) {
        super(module);
    }

}
