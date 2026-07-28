package net.flectone.pulse.module.command.maintenance.model;

import net.flectone.pulse.model.event.message.context.ModerationMessageContext;

public interface MaintenanceMessageContext extends ModerationMessageContext {

    static MaintenanceMessageContextImpl.MaintenanceMessageContextImplBuilder builder() {
        return MaintenanceMessageContextImpl.builder();
    }

    boolean turned();

    MaintenanceMessageContext withTurned(boolean turned);

}