package net.flectone.pulse.module.integration.discord.listener;

import discord4j.core.event.domain.Event;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.module.AbstractModuleMessage;
import reactor.core.publisher.Mono;

public abstract class EventListener<T extends Event> extends AbstractModuleMessage<Localization.Integration.Discord> {

    protected EventListener() {
        super(localization -> localization.getIntegration().getDiscord());
    }

    public abstract Class<T> getEventType();

    public abstract Mono<T> execute(T event);

}
