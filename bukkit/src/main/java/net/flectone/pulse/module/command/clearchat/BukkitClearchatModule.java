package net.flectone.pulse.module.command.clearchat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.BukkitCommandUtil;

@Singleton
public class BukkitClearchatModule extends ClearchatModule {

    private final BukkitCommandUtil commandManager;

    @Inject
    public BukkitClearchatModule(FPlayerManager fPlayerManager,
                                 FileManager fileManager,
                                 BukkitCommandUtil commandManager) {
        super(fPlayerManager, fileManager, commandManager);

        this.commandManager = commandManager;
    }

    @Override
    public void createCommand() {
        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new StringArgument("player")
                        .withPermission(getPermission().getOther().getName())
                        .includeSuggestions(commandManager.argumentFPlayers(false))
                        .executesPlayer(this::executesFPlayer)
                )
                .then(new EntitySelectorArgument.ManyPlayers("players")
                        .withPermission(getPermission().getOther().getName())
                        .executes(this::executesFPlayer))
                .executesPlayer(this::executesFPlayer)
                .override();
    }
}
