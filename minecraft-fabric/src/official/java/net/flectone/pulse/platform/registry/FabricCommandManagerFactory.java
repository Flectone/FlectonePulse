package net.flectone.pulse.platform.registry;

import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FPlayer;
import net.minecraft.commands.CommandSourceStack;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.fabric.FabricServerCommandManager;

@Singleton
public class FabricCommandManagerFactory {

    public CommandManager<FPlayer> create(ExecutionCoordinator<FPlayer> executionCoordinator, SenderMapper<CommandSourceStack, FPlayer> senderMapper) {
        return new FabricServerCommandManager<>(executionCoordinator, senderMapper);
    }

}
