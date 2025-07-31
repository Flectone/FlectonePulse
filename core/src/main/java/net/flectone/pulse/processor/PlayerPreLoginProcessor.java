package net.flectone.pulse.processor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.dispatcher.EventDispatcher;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.player.PlayerPreLoginEvent;
import net.flectone.pulse.registry.ProxyRegistry;
import net.flectone.pulse.service.FPlayerService;

import java.util.UUID;
import java.util.function.Consumer;

@Singleton
public class PlayerPreLoginProcessor {

    private final FPlayerService fPlayerService;
    private final ProxyRegistry proxyRegistry;
    private final EventDispatcher eventDispatcher;

    @Inject
    public PlayerPreLoginProcessor(FPlayerService fPlayerService,
                                   ProxyRegistry proxyRegistry,
                                   EventDispatcher eventDispatcher) {
        this.fPlayerService = fPlayerService;
        this.proxyRegistry = proxyRegistry;
        this.eventDispatcher = eventDispatcher;
    }

    @Async
    public void processAsyncLogin(UUID uuid, String name, Consumer<PlayerPreLoginEvent> allowedConsumer, Consumer<PlayerPreLoginEvent> kickConsumer) {
        processLogin(uuid, name, allowedConsumer, kickConsumer);
    }

    public void processLogin(UUID uuid, String name, Consumer<PlayerPreLoginEvent> allowedConsumer, Consumer<PlayerPreLoginEvent> kickConsumer) {
        // if no one was on the server, the cache may be invalid for other servers
        // because FlectonePulse on Proxy cannot send a message for servers that have no player
        if (fPlayerService.getFPlayers().isEmpty() && proxyRegistry.hasEnabledProxy()) {
            // clears the cache of players who might have left from other servers
            fPlayerService.clear();
        }

        FPlayer fPlayer = fPlayerService.addFPlayer(uuid, name);
        PlayerPreLoginEvent event = new PlayerPreLoginEvent(fPlayer);
        eventDispatcher.dispatch(event);

        if (event.isAllowed()) {
            fPlayerService.loadData(uuid);
            allowedConsumer.accept(event);
        } else {
            kickConsumer.accept(event);
        }
    }

}
