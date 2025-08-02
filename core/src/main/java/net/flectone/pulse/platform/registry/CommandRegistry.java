package net.flectone.pulse.platform.registry;

import net.flectone.pulse.model.entity.FPlayer;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

import java.util.function.Function;

public abstract class CommandRegistry implements Registry {

    protected CommandRegistry() {
    }

    public abstract void registerCommand(Function<CommandManager<FPlayer> , Command.Builder<FPlayer>> builder);

    public abstract void unregisterCommand(String name);

}
