package net.flectone.pulse.module.message.bubble;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BubbleModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final BubbleService bubbleService;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        bubbleService.startTicker();

        listenerRegistry.register(BubblePacketListener.class);
        listenerRegistry.register(BubblePulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        bubbleService.clear();
    }

    @Override
    public Message.Bubble config() {
        return fileResolver.getMessage().getBubble();
    }

    @Override
    public Permission.Message.Bubble permission() {
        return fileResolver.getPermission().getMessage().getBubble();
    }

    @Async
    public void add(@NotNull FPlayer fPlayer, @NotNull String inputString, List<FPlayer> receivers) {
        if (isModuleDisabledFor(fPlayer)) return;

        bubbleService.addMessage(fPlayer, inputString, receivers);
    }

    public enum Billboard {
        FIXED,
        VERTICAL,
        HORIZONTAL,
        CENTER
    }
}
