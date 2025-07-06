package net.flectone.pulse.module.message.afk;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.afk.listener.AfkListener;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import org.bukkit.event.EventPriority;

@Singleton
public class BukkitAfkModule extends AfkModule {

    private final BukkitListenerRegistry bukkitListenerManager;

    @Inject
    public BukkitAfkModule(FileResolver fileResolver,
                           MessageProcessRegistry messageProcessRegistry,
                           FPlayerService fPlayerService,
                           TaskScheduler taskScheduler,
                           IntegrationModule integrationModule,
                           PermissionChecker permissionChecker,
                           PlatformPlayerAdapter platformPlayerAdapter,
                           BukkitListenerRegistry bukkitListenerRegistry) {
        super(fileResolver, messageProcessRegistry, fPlayerService, taskScheduler, integrationModule, permissionChecker, platformPlayerAdapter);

        this.bukkitListenerManager = bukkitListenerRegistry;
    }

    @Override
    public void reload() {
        super.reload();

        bukkitListenerManager.register(AfkListener.class, EventPriority.NORMAL);
    }
}
