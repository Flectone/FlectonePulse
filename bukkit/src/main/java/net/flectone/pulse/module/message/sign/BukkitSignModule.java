package net.flectone.pulse.module.message.sign;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.message.sign.listener.SignListener;
import net.flectone.pulse.util.ComponentUtil;
import org.bukkit.event.EventPriority;

@Singleton
public class BukkitSignModule extends SignModule {

    private final BukkitListenerRegistry bukkitListenerManager;

    @Inject
    public BukkitSignModule(FileManager fileManager,
                            ComponentUtil componentUtil,
                            BukkitListenerRegistry bukkitListenerManager) {
        super(fileManager, componentUtil);

        this.bukkitListenerManager = bukkitListenerManager;
    }

    @Override
    public void reload() {
        super.reload();

        bukkitListenerManager.register(SignListener.class, EventPriority.NORMAL);
    }
}
