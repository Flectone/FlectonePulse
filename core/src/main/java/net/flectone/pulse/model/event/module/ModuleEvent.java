package net.flectone.pulse.model.event.module;

import lombok.Getter;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.module.AbstractModule;

@Getter
public abstract class ModuleEvent extends Event {

    private final AbstractModule module;

    protected ModuleEvent(AbstractModule module) {
        this.module = module;
    }

}
