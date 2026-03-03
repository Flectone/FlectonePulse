package net.flectone.pulse.module.integration.twitch.listener;

import com.github.twitch4j.chat.events.TwitchEvent;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.util.constant.ModuleName;

public interface EventListener<T extends TwitchEvent> extends ModuleLocalization<Localization.Integration.Twitch> {

    Class<T> getEventType();

    void execute(T event);

    @Override
    default ModuleName name() {
        return ModuleName.INTEGRATION_TWITCH;
    }

}
