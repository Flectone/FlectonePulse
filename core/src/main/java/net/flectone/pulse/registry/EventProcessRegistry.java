package net.flectone.pulse.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.TranslatableMessageEvent;
import net.flectone.pulse.model.event.player.PlayerEvent;

import java.util.*;
import java.util.function.Consumer;

@Singleton
public class EventProcessRegistry implements Registry {

    private final Map<Event.Type, List<Consumer<Event>>> handlers = new EnumMap<>(Event.Type.class);

    @Inject
    public EventProcessRegistry() {
    }

    public void registerHandler(Event.Type type, Consumer<Event> handler) {
        handlers.computeIfAbsent(type, k -> new ArrayList<>()).add(handler);
    }

    public void registerPlayerHandler(Event.Type type, Consumer<FPlayer> handler) {
        registerHandler(type, event -> handler.accept(((PlayerEvent) event).getPlayer()));
    }

    public void registerMessageHandler(Consumer<TranslatableMessageEvent> handler) {
        registerHandler(Event.Type.TRANSLATABLE_MESSAGE_RECEIVE, event -> handler.accept((TranslatableMessageEvent) event));
    }

    public void processEvent(Event event) {
        handlers.getOrDefault(event.getType(), Collections.emptyList())
                .forEach(handler -> handler.accept(event));
    }

    @Override
    public void reload() {
        handlers.clear();
    }

}
