package net.flectone.pulse.module.message.bubble;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.manager.BukkitListenerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.bubble.listener.BubbleListener;
import net.flectone.pulse.module.message.bubble.manager.BukkitBubbleManager;
import net.flectone.pulse.module.message.chat.BukkitChatModule;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

@Singleton
public class BukkitBubbleModule extends BubbleModule {

    private final BukkitBubbleManager bubbleManager;
    private final BukkitListenerManager bukkitListenerManager;

    @Inject private BukkitChatModule chatModule;

    @Inject
    public BukkitBubbleModule(FileManager fileManager,
                              BukkitBubbleManager bubbleManager,
                              BukkitListenerManager bukkitListenerManager) {
        super(fileManager);
        this.bubbleManager = bubbleManager;
        this.bukkitListenerManager = bukkitListenerManager;
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
