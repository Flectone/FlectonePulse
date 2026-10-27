package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.mapper.NeoForgeFPlayerMapper;
import net.flectone.pulse.platform.handler.CommandExceptionHandler;
import net.flectone.pulse.platform.registry.cloud.BrigadierRegistrationHandler;
import net.flectone.pulse.platform.registry.cloud.PlatformRegistrationHandler;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Singleton
public class NeoForgeCommandRegistry extends CommandRegistryImpl<CommandSourceStack> implements BrigadierCommandRegistry {

    private BrigadierRegistrationHandler<CommandSourceStack> registrationHandler;

    @Inject
    public NeoForgeCommandRegistry(PermissionChecker permissionChecker,
                                   CommandExceptionHandler commandExceptionHandler,
                                   TaskScheduler taskScheduler,
                                   NeoForgeFPlayerMapper fPlayerMapper) {
        super(permissionChecker, commandExceptionHandler, taskScheduler, fPlayerMapper);
    }

    @Override
    protected PlatformRegistrationHandler<CommandSourceStack> createRegistrationHandler() {
        this.registrationHandler = new BrigadierRegistrationHandler<>(registrar ->
                NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> registrar.accept(event.getDispatcher()))
        );

        return registrationHandler;
    }

    @Override
    public void init() {
        if (this.manager != null) return;

        super.init();

        setupBrigadierManager(registrationHandler.getBrigadierManager());
    }

}
