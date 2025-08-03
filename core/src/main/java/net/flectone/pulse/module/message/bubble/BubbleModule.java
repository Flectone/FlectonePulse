package net.flectone.pulse.module.message.bubble;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.bubble.listener.BubblePacketListener;
import net.flectone.pulse.module.message.bubble.listener.BubblePulseListener;
import net.flectone.pulse.module.message.bubble.service.BubbleService;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import org.jetbrains.annotations.NotNull;

@Singleton
public class BubbleModule extends AbstractModule {

    private final Message.Bubble message;
    private final Permission.Message.Bubble permission;
    private final BubbleService bubbleService;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public BubbleModule(FileResolver fileResolver,
                        BubbleService bubbleService,
                        ListenerRegistry listenerRegistry) {
        this.message = fileResolver.getMessage().getBubble();
        this.permission = fileResolver.getPermission().getMessage().getBubble();
        this.bubbleService = bubbleService;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        bubbleService.startTicker();

        registerModulePermission(permission);

        listenerRegistry.register(BubblePacketListener.class);
        listenerRegistry.register(BubblePulseListener.class);
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
        if (isModuleDisabledFor(fPlayer)) return;

        bubbleService.addMessage(fPlayer, inputString);
    }

    public enum Billboard {
        FIXED,
        VERTICAL,
        HORIZONTAL,
        CENTER
    }
}
