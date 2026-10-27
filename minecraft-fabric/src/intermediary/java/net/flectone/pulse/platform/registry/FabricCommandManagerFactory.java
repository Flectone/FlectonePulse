package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.adapter.FabricVersionAdapter;
import net.minecraft.commands.CommandSourceStack;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricCommandManagerFactory {

    private final FabricVersionAdapter fabricVersionAdapter;

    public CommandManager<FPlayer> create(ExecutionCoordinator<FPlayer> executionCoordinator, SenderMapper<CommandSourceStack, FPlayer> senderMapper) {
        return new FabricBrigadierCommandManager(executionCoordinator, senderMapper, fabricVersionAdapter);
    }

}
