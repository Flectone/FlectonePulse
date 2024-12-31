package net.flectone.pulse.module.command.ban;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.*;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.*;

@Singleton
public class BukkitBanModule extends BanModule {

    private final BukkitCommandUtil commandManager;

    @Inject
    public BukkitBanModule(Database database,
                           FileManager fileManager,
                           FPlayerManager fPlayerManager,
                           PermissionUtil permissionUtil,
                           ThreadManager threadManager,
                           ListenerManager listenerManager,
                           BukkitCommandUtil commandManager,
                           ComponentUtil componentUtil,
                           PacketEventsUtil packetEventsUtil,
                           TimeUtil timeUtil,
                           FLogger fLogger,
                           Gson gson) {
        super(database, fileManager, fPlayerManager, permissionUtil, threadManager, listenerManager, commandManager,
                componentUtil, packetEventsUtil, timeUtil, fLogger, gson);

        this.commandManager = commandManager;
    }

    @Override
    public void createCommand() {
        String name = getCommand().getAliases().get(0);
        String promptPlayer = getPrompt().getPlayer();
        String promptReason = getPrompt().getReason();
        String promptTime = getPrompt().getTime();

        new FCommand(name)
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new StringArgument(promptPlayer)
                        .includeSuggestions(commandManager.argumentFPlayers(getCommand().isSuggestOfflinePlayers()))
                        .then(commandManager.timeArgument(promptTime)
                                .then(new GreedyStringArgument(promptReason).setOptional(true)
                                        .executes(this::executesFPlayer)
                                )
                        )
                        .then(new GreedyStringArgument(promptReason).setOptional(true)
                                .executes(this::executesFPlayer)
                        )
                )
                .override();
    }
}
