package net.flectone.pulse.module.integration.twitch.listener;

import com.github.twitch4j.chat.events.TwitchEvent;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.util.constant.ModuleName;

public abstract class EventListener<T extends TwitchEvent> implements AbstractModuleLocalization<Localization.Integration.Twitch> {

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_TWITCH;
    }

    public abstract Class<T> getEventType();

    public abstract void execute(T event);

}
