package net.flectone.pulse.module.command.tictactoe;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ProxyManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.module.command.tictactoe.manager.TictactoeManager;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.BukkitCommandUtil;

@Singleton
public class BukkitTictactoeModule extends TictactoeModule {

    private final FPlayerManager fPlayerManager;
    private final BukkitCommandUtil commandUtil;

    @Inject
    public BukkitTictactoeModule(FileManager fileManager,
                                 FPlayerDAO fPlayerDAO,
                                 TictactoeManager tictactoeManager,
                                 FPlayerManager fPlayerManager,
                                 ProxyManager proxyManager,
                                 IntegrationModule integrationModule,
                                 BukkitCommandUtil commandUtil,
                                 Gson gson) {
        super(fileManager, fPlayerDAO, tictactoeManager, proxyManager, integrationModule, commandUtil, gson);

        this.fPlayerManager = fPlayerManager;
        this.commandUtil = commandUtil;
    }

    @Override
    public void createCommand() {
        String playerPrompt = getPrompt().getPlayer();
        String hardPrompt = getPrompt().getHard();
        String promptId = getPrompt().getId();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new StringArgument(playerPrompt)
                        .includeSuggestions(commandUtil.argumentFPlayers(false))
                        .then(new BooleanArgument(hardPrompt).setOptional(true)
                                .executesPlayer(this::executesFPlayer)
                        )
                )
                .then(new IntegerArgument(promptId + " 1")
                        .then(new StringArgument(promptId + " 2")
                                .executesPlayer((player, args) -> {
                                    move(fPlayerManager.get(player), args);
                                })
                        )
                )
                .override();
    }
}
