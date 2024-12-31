package net.flectone.pulse.module.command.rockpaperscissors;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.UUIDArgument;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ProxyManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.BukkitCommandUtil;

@Singleton
public class BukkitRockpaperscissorsModule extends RockpaperscissorsModule {

    private final BukkitCommandUtil commandUtil;

    @Inject
    public BukkitRockpaperscissorsModule(FileManager fileManager,
                                         ProxyManager proxyManager,
                                         Database database,
                                         BukkitCommandUtil commandUtil,
                                         IntegrationModule integrationModule) {
        super(fileManager, proxyManager, database, commandUtil, integrationModule);

        this.commandUtil = commandUtil;
    }

    @Override
    public void createCommand() {
        String name = getCommand().getAliases().get(0);
        String playerPrompt = getPrompt().getPlayer();
        String movePrompt = getPrompt().getMove();

        new FCommand(name)
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new StringArgument(playerPrompt)
                        .includeSuggestions(commandUtil.argumentFPlayers(false))
                        .executes(this::executesFPlayerDatabase)
                        .then(new StringArgument(movePrompt)
                                .then(new UUIDArgument("uuid")
                                        .then(new BooleanArgument("boolean")
                                                .setOptional(true)
                                                .executes(this::executesFPlayerDatabase)
                                        )
                                )
                        )
                )
                .override();
    }
}
