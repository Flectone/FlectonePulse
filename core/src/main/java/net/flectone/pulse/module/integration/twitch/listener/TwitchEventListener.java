package net.flectone.pulse.module.integration.twitch.listener;

import com.github.twitch4j.chat.events.TwitchEvent;

public interface TwitchEventListener<T extends TwitchEvent> {

    Class<T> getEventType();

    void execute(T event);

}
