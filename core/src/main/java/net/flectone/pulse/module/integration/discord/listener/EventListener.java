package net.flectone.pulse.module.integration.discord.listener;

import discord4j.core.event.domain.Event;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.util.constant.ModuleName;
import org.jspecify.annotations.NonNull;
import reactor.core.publisher.Mono;

public interface  EventListener<T extends Event> extends ModuleLocalization<Localization.Integration.Discord> {

    Class<T> getEventType();

    Mono<@NonNull T> execute(T event);

    @Override
    default ModuleName name() {
        return ModuleName.INTEGRATION_DISCORD;
    }

}
