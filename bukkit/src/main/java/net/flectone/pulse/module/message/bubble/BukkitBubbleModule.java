package net.flectone.pulse.module.message.bubble;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
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
    public BukkitBubbleModule(FileManager fileManager,
                              BubbleService bubbleService,
                              BukkitListenerRegistry bukkitListenerRegistry,
                              ChatModule bukkitChatModule) {
        super(fileManager, bubbleService);

        this.bukkitListenerRegistry = bukkitListenerRegistry;
        this.chatModule = bukkitChatModule;
    }

    @Override
    public void reload() {
        super.reload();

        if (!chatModule.isEnable()) {
            bukkitListenerRegistry.register(BubbleListener.class, EventPriority.MONITOR);
        }
    }
}
