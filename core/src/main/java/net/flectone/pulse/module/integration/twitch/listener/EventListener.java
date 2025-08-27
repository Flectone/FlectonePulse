package net.flectone.pulse.module.integration.twitch.listener;

import com.github.twitch4j.chat.events.TwitchEvent;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.util.constant.MessageType;

public abstract class EventListener<T extends TwitchEvent>  extends AbstractModuleLocalization<Localization.Integration.Twitch> {

    protected EventListener() {
        super(module -> module.getIntegration().getTwitch(), MessageType.FROM_TWITCH_TO_MINECRAFT);
    }

    public abstract Class<T> getEventType();

    public abstract void execute(T event);

}
