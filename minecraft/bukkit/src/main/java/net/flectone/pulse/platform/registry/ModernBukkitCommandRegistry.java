package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.platform.handler.CommandExceptionHandler;
import net.flectone.pulse.processing.mapper.FPlayerMapper;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.file.FileFacade;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;

@Singleton
public class ModernBukkitCommandRegistry extends LegacyBukkitCommandRegistry {

    @Inject
    public ModernBukkitCommandRegistry(FileFacade fileFacade,
                                       ReflectionResolver reflectionResolver,
                                       CommandExceptionHandler commandExceptionHandler,
                                       Plugin plugin,
                                       TaskScheduler taskScheduler,
                                       FPlayerMapper fPlayerMapper) {
        super(fileFacade, commandExceptionHandler, plugin, reflectionResolver, taskScheduler, fPlayerMapper);
    }

    @Override
    public void init() {
        super.init();

        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            manager.registerBrigadier();
            setupBrigadierManager(manager.brigadierManager());
        } else if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }
    }
}
