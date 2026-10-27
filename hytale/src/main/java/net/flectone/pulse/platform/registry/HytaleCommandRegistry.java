package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.mapper.HytaleFPlayerMapper;
import net.flectone.pulse.platform.handler.CommandExceptionHandler;
import net.flectone.pulse.platform.registry.cloud.HytaleRegistrationHandler;
import net.flectone.pulse.platform.registry.cloud.PlatformRegistrationHandler;
import net.flectone.pulse.scheduler.TaskScheduler;

@Singleton
public class HytaleCommandRegistry extends CommandRegistryImpl<CommandSender> {

    private final JavaPlugin javaPlugin;

    @Inject
    public HytaleCommandRegistry(PermissionChecker permissionChecker,
                                 CommandExceptionHandler commandExceptionHandler,
                                 TaskScheduler taskScheduler,
                                 HytaleFPlayerMapper fPlayerMapper,
                                 JavaPlugin javaPlugin) {
        super(permissionChecker, commandExceptionHandler, taskScheduler, fPlayerMapper);

        this.javaPlugin = javaPlugin;
    }

    @Override
    protected PlatformRegistrationHandler<CommandSender> createRegistrationHandler() {
        return new HytaleRegistrationHandler(javaPlugin);
    }

}
