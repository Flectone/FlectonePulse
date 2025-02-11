package net.flectone.pulse.module.command.rockpaperscissors;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.UUIDArgument;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.connector.ProxyConnector;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.BukkitCommandUtil;

@Singleton
public class BukkitRockpaperscissorsModule extends RockpaperscissorsModule {

    private final BukkitCommandUtil commandUtil;

    @Inject
    public BukkitRockpaperscissorsModule(FileManager fileManager,
                                         ProxyConnector proxyConnector,
                                         FPlayerDAO FPlayerDAO,
                                         BukkitCommandUtil commandUtil,
                                         IntegrationModule integrationModule) {
        super(fileManager, proxyConnector, FPlayerDAO, commandUtil, integrationModule);

        this.commandUtil = commandUtil;
    }

    @Override
    public void createCommand() {
        String playerPrompt = getPrompt().getPlayer();
        String movePrompt = getPrompt().getMove();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new StringArgument(playerPrompt)
                        .includeSuggestions(commandUtil.argumentFPlayers(false))
                        .executes(this::executesFPlayer)
                        .then(new StringArgument(movePrompt)
                                .then(new UUIDArgument("uuid")
                                        .then(new BooleanArgument("boolean")
                                                .setOptional(true)
                                                .executes(this::executesFPlayer)
                                        )
                                )
                        )
                )
                .override();
    }
}
