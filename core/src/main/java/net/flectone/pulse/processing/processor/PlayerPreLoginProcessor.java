package net.flectone.pulse.processing.processor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerPreLoginEvent;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.service.FPlayerService;

import java.util.UUID;
import java.util.function.Consumer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerPreLoginProcessor {

    private final FPlayerService fPlayerService;
    private final ProxyRegistry proxyRegistry;
    private final EventDispatcher eventDispatcher;

    @Async
    public void processAsyncLogin(UUID uuid, String name, Consumer<PlayerPreLoginEvent> allowedConsumer, Consumer<PlayerPreLoginEvent> kickConsumer) {
        processLogin(uuid, name, allowedConsumer, kickConsumer);
    }

    public void processLogin(UUID uuid, String name, Consumer<PlayerPreLoginEvent> allowedConsumer, Consumer<PlayerPreLoginEvent> kickConsumer) {
        // if no one was on the server, the cache may be invalid for other servers
        // because FlectonePulse on Proxy cannot send a message for servers that have no player
        if (fPlayerService.getOnlineFPlayers().isEmpty() && proxyRegistry.hasEnabledProxy()) {
            // clears the cache of players who might have left from other servers
            fPlayerService.clear();
            fPlayerService.addConsole();
        }

        FPlayer fPlayer = fPlayerService.addFPlayer(uuid, name);
        PlayerPreLoginEvent event = new PlayerPreLoginEvent(fPlayer);
        eventDispatcher.dispatch(event);

        if (event.isAllowed()) {
            fPlayerService.loadData(fPlayer);
            allowedConsumer.accept(event);
        } else {
            kickConsumer.accept(event);
        }
    }
}
