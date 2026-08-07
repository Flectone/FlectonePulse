package net.flectone.pulse.module.command.maintenance.model;

import net.flectone.pulse.model.event.message.context.ModerationMessageContext;

/**
 * A change to the server's maintenance state.
 * Builds on {@link ModerationMessageContext}.
 * @author TheFaser
 */
public interface MaintenanceMessageContext extends ModerationMessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static MaintenanceMessageContextImpl.MaintenanceMessageContextImplBuilder builder() {
        return MaintenanceMessageContextImpl.builder();
    }

    /**
     * Whether maintenance was switched on, rather than off.
     *
     * @return whether maintenance was switched on, rather than off
     */
    boolean turned();

    /**
     * Returns a copy carrying a different value.
     *
     * @param turned whether maintenance was switched on, rather than off
     * @return the copy
     */
    MaintenanceMessageContext withTurned(boolean turned);

}