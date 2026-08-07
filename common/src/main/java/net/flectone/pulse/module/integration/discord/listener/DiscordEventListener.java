package net.flectone.pulse.module.integration.discord.listener;

import discord4j.core.event.domain.Event;
import org.jspecify.annotations.NonNull;
import reactor.core.publisher.Mono;

public interface DiscordEventListener<T extends Event> {

    Class<T> getEventType();

    Mono<@NonNull T> execute(T event);

}
