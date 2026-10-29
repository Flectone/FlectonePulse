package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.SpongeFlectonePulse;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.mapper.SpongeFPlayerMapper;
import net.flectone.pulse.platform.handler.CommandExceptionHandler;
import net.flectone.pulse.platform.registry.cloud.PlatformRegistrationHandler;
import net.flectone.pulse.platform.registry.cloud.SpongeRegistrationHandler;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.minecraft.commands.CommandSourceStack;

@Singleton
public class SpongeCommandRegistry extends CommandRegistryImpl<CommandSourceStack> implements BrigadierCommandRegistry {

    private final SpongeFlectonePulse spongeFlectonePulse;

    private SpongeRegistrationHandler registrationHandler;

    @Inject
    public SpongeCommandRegistry(PermissionChecker permissionChecker,
                                 CommandExceptionHandler commandExceptionHandler,
                                 TaskScheduler taskScheduler,
                                 SpongeFPlayerMapper fPlayerMapper,
                                 SpongeFlectonePulse spongeFlectonePulse) {
        super(permissionChecker, commandExceptionHandler, taskScheduler, fPlayerMapper);

        this.spongeFlectonePulse = spongeFlectonePulse;
    }

    @Override
    protected PlatformRegistrationHandler<CommandSourceStack> createRegistrationHandler() {
        this.registrationHandler = new SpongeRegistrationHandler(spongeFlectonePulse.getContainer());
        return registrationHandler;
    }

    @Override
    public void init() {
        if (this.manager != null) return;

        super.init();

        setupBrigadierManager(registrationHandler.getBrigadierManager());
    }

}
