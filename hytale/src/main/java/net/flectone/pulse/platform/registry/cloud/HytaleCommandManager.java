package net.flectone.pulse.platform.registry.cloud;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import lombok.Getter;
import net.flectone.pulse.model.entity.FPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;

public class HytaleCommandManager extends CommandManager<FPlayer> {

    @Getter
    private final SenderMapper<CommandSender, FPlayer> senderMapper;

    public HytaleCommandManager(@NonNull ExecutionCoordinator<FPlayer> executionCoordinator,
                                   @NonNull HytaleRegistrationHandler commandRegistrationHandler,
                                   @NonNull SenderMapper<CommandSender, FPlayer> senderMapper,
                                   JavaPlugin javaPlugin) {
        super(executionCoordinator, commandRegistrationHandler);

        this.senderMapper = senderMapper;

        commandRegistrationHandler.initialize(this, javaPlugin);
    }

    @Override
    public boolean hasPermission(@NonNull FPlayer sender, @NonNull String permission) {
        return PermissionsModule.get().hasPermission(sender.getUuid(), permission);
    }

}
