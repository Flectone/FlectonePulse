package net.flectone.pulse.module.message.bubble;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.bubble.listener.BubbleListener;
import net.flectone.pulse.module.message.bubble.service.BubbleService;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;
import org.jetbrains.annotations.NotNull;

@Singleton
public class BubbleModule extends AbstractModule {

    private final Message.Bubble message;
    private final Permission.Message.Bubble permission;
    private final BubbleService bubbleService;
    private final EventProcessRegistry eventProcessRegistry;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public BubbleModule(FileResolver fileResolver,
                        BubbleService bubbleService,
                        EventProcessRegistry eventProcessRegistry,
                        ListenerRegistry listenerRegistry) {
        this.message = fileResolver.getMessage().getBubble();
        this.permission = fileResolver.getPermission().getMessage().getBubble();
        this.bubbleService = bubbleService;
        this.eventProcessRegistry = eventProcessRegistry;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        bubbleService.startTicker();

        registerModulePermission(permission);

        listenerRegistry.register(BubbleListener.class);

        eventProcessRegistry.registerPlayerHandler(Event.Type.PLAYER_QUIT, bubbleService::clear);
    }

    @Override
    public void onDisable() {
        bubbleService.clear();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void add(@NotNull FPlayer fPlayer, @NotNull String inputString) {
        if (checkModulePredicates(fPlayer)) return;

        bubbleService.addMessage(fPlayer, inputString);
    }

    public enum Billboard {
        FIXED,
        VERTICAL,
        HORIZONTAL,
        CENTER
    }
}
