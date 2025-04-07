package net.flectone.pulse.module.message.bubble;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.bubble.listener.BubbleListener;
import net.flectone.pulse.module.message.bubble.manager.BukkitBubbleManager;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

@Singleton
public class BukkitBubbleModule extends BubbleModule {

    private final BukkitBubbleManager bubbleManager;
    private final BukkitListenerRegistry bukkitListenerManager;

    private final ChatModule chatModule;

    @Inject
    public BukkitBubbleModule(FileManager fileManager,
                              BukkitBubbleManager bubbleManager,
                              BukkitListenerRegistry bukkitListenerManager,
                              ChatModule bukkitChatModule) {
        super(fileManager);
        this.bubbleManager = bubbleManager;
        this.bukkitListenerManager = bukkitListenerManager;
        this.chatModule = bukkitChatModule;
    }

    @Override
    public void reload() {
        super.reload();

        if (!chatModule.isEnable()) {
            bukkitListenerManager.register(BubbleListener.class, EventPriority.MONITOR);
        }
    }

    @Async
    @Override
    public void add(@NotNull FPlayer fPlayer, @NotNull String inputString) {
        if (checkModulePredicates(fPlayer)) return;

        bubbleManager.add(fPlayer, inputString);
    }
}
