package net.flectone.pulse.module.integration.twitch.listener;

import com.github.twitch4j.chat.events.TwitchEvent;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.module.AbstractModuleLocalization;

public abstract class EventListener<T extends TwitchEvent>  extends AbstractModuleLocalization<Localization.Integration.Twitch> {

    protected EventListener() {
        super(module -> module.getIntegration().getTwitch());
    }

    public abstract Class<T> getEventType();

    public abstract void execute(T event);

}
