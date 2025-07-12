package net.flectone.pulse.module.message.bubble;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.module.message.bubble.listener.BubbleListener;
import net.flectone.pulse.module.message.bubble.service.BubbleService;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import org.bukkit.event.EventPriority;

@Singleton
public class BukkitBubbleModule extends BubbleModule {

    private final BukkitListenerRegistry bukkitListenerRegistry;
    private final ChatModule chatModule;

    @Inject
    public BukkitBubbleModule(FileResolver fileResolver,
                              BubbleService bubbleService,
                              BukkitListenerRegistry bukkitListenerRegistry,
                              ChatModule bukkitChatModule) {
        super(fileResolver, bubbleService);

        this.bukkitListenerRegistry = bukkitListenerRegistry;
        this.chatModule = bukkitChatModule;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (!chatModule.isEnable()) {
            bukkitListenerRegistry.register(BubbleListener.class, EventPriority.MONITOR);
        }
    }
}
