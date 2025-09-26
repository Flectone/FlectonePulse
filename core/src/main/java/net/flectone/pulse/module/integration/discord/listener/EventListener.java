package net.flectone.pulse.module.integration.discord.listener;

import discord4j.core.event.domain.Event;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.util.constant.MessageType;
import reactor.core.publisher.Mono;

public abstract class EventListener<T extends Event> extends AbstractModuleLocalization<Localization.Integration.Discord> {

    protected EventListener() {
        super(MessageType.FROM_DISCORD_TO_MINECRAFT);
    }

    public abstract Class<T> getEventType();

    public abstract Mono<T> execute(T event);

}
