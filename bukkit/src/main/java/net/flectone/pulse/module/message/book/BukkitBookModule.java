package net.flectone.pulse.module.message.book;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.BukkitListenerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.message.book.listener.BookListener;
import net.flectone.pulse.util.ComponentUtil;
import org.bukkit.event.EventPriority;

@Singleton
public class BukkitBookModule extends BookModule {

    private final BukkitListenerManager bukkitListenerManager;

    @Inject
    public BukkitBookModule(FileManager fileManager,
                            BukkitListenerManager bukkitListenerManager,
                            ComponentUtil componentUtil) {
        super(fileManager, componentUtil);

        this.bukkitListenerManager = bukkitListenerManager;
    }

    @Override
    public void reload() {
        super.reload();

        bukkitListenerManager.register(BookListener.class, EventPriority.NORMAL);
    }
}
