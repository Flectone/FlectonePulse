package net.flectone.pulse.module.integration.twitch.listener;

import com.github.twitch4j.chat.events.TwitchEvent;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.module.AbstractModuleMessage;

public abstract class EventListener<T extends TwitchEvent>  extends AbstractModuleMessage<Localization.Integration.Twitch> {

    public EventListener() {
        super(module -> module.getIntegration().getTwitch());
    }

    public abstract Class<T> getEventType();

    public abstract void execute(T event);
}
