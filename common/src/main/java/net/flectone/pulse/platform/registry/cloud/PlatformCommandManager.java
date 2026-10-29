package net.flectone.pulse.platform.registry.cloud;

import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.model.entity.FPlayer;
import org.incendo.cloud.CloudCapability;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.SenderMapperHolder;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.jspecify.annotations.NonNull;

public class PlatformCommandManager<S> extends CommandManager<FPlayer> implements SenderMapperHolder<S, FPlayer> {

    private final SenderMapper<S, FPlayer> senderMapper;
    private final PermissionChecker permissionChecker;

    public PlatformCommandManager(ExecutionCoordinator<FPlayer> executionCoordinator,
                                  PlatformRegistrationHandler<S> registrationHandler,
                                  SenderMapper<S, FPlayer> senderMapper,
                                  PermissionChecker permissionChecker) {
        super(executionCoordinator, registrationHandler);

        this.senderMapper = senderMapper;
        this.permissionChecker = permissionChecker;

        registerCapability(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION);
        registrationHandler.initialize(this);
    }

    @Override
    public boolean hasPermission(@NonNull FPlayer sender, @NonNull String permission) {
        return permissionChecker.check(sender, permission);
    }

    @Override
    public @NonNull SenderMapper<S, FPlayer> senderMapper() {
        return senderMapper;
    }

}
