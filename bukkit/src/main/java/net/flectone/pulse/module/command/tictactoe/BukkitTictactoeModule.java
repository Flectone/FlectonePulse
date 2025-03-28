package net.flectone.pulse.module.command.tictactoe;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.connector.ProxyConnector;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.module.command.tictactoe.manager.TictactoeManager;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.BukkitCommandUtil;

@Singleton
public class BukkitTictactoeModule extends TictactoeModule {

    private final FPlayerService fPlayerService;
    private final BukkitCommandUtil commandUtil;

    @Inject
    public BukkitTictactoeModule(FileManager fileManager,
                                 FPlayerService fPlayerService,
                                 TictactoeManager tictactoeManager,
                                 ProxyConnector proxyConnector,
                                 IntegrationModule integrationModule,
                                 BukkitCommandUtil commandUtil,
                                 Gson gson) {
        super(fileManager, fPlayerService, tictactoeManager, proxyConnector, integrationModule, commandUtil, gson);

        this.fPlayerService = fPlayerService;
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
                                    move(fPlayerService.getFPlayer(player), args);
                                })
                        )
                )
                .override();
    }
}
