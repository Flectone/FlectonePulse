package net.flectone.pulse.module.integration.discord.listener;

import discord4j.core.event.domain.Event;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.util.constant.ModuleName;
import reactor.core.publisher.Mono;

public abstract class EventListener<T extends Event> implements ModuleLocalization<Localization.Integration.Discord> {

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_DISCORD;
    }

    public abstract Class<T> getEventType();

    public abstract Mono<T> execute(T event);

}
