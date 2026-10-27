package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.mapper.FabricFPlayerMapper;
import net.flectone.pulse.platform.adapter.FabricVersionAdapter;
import net.flectone.pulse.platform.handler.CommandExceptionHandler;
import net.flectone.pulse.platform.registry.cloud.BrigadierRegistrationHandler;
import net.flectone.pulse.platform.registry.cloud.PlatformRegistrationHandler;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.minecraft.commands.CommandSourceStack;

@Singleton
public class FabricCommandRegistry extends CommandRegistryImpl<CommandSourceStack> implements BrigadierCommandRegistry {

    private final FabricVersionAdapter fabricVersionAdapter;

    private BrigadierRegistrationHandler<CommandSourceStack> registrationHandler;

    @Inject
    public FabricCommandRegistry(PermissionChecker permissionChecker,
                                 CommandExceptionHandler commandExceptionHandler,
                                 TaskScheduler taskScheduler,
                                 FabricFPlayerMapper fPlayerMapper,
                                 FabricVersionAdapter fabricVersionAdapter) {
        super(permissionChecker, commandExceptionHandler, taskScheduler, fPlayerMapper);

        this.fabricVersionAdapter = fabricVersionAdapter;
    }

    @Override
    protected PlatformRegistrationHandler<CommandSourceStack> createRegistrationHandler() {
        this.registrationHandler = new BrigadierRegistrationHandler<>(fabricVersionAdapter::registerCommands);
        return registrationHandler;
    }

    @Override
    public void init() {
        if (this.manager != null) return;

        super.init();

        setupBrigadierManager(registrationHandler.getBrigadierManager());
    }

}
