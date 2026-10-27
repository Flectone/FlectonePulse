package net.flectone.pulse.platform.registry.cloud;

import net.flectone.pulse.model.entity.FPlayer;
import org.incendo.cloud.internal.CommandRegistrationHandler;

public abstract class PlatformRegistrationHandler<S> implements CommandRegistrationHandler<FPlayer> {

    protected PlatformCommandManager<S> manager;

    public void initialize(PlatformCommandManager<S> manager) {
        this.manager = manager;
    }

}
