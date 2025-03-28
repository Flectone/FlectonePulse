package net.flectone.pulse.module.command.tell;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.connector.ProxyConnector;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.BukkitCommandUtil;

@Singleton
public class BukkitTellModule extends TellModule {

    private final BukkitCommandUtil commandUtil;

    @Inject
    public BukkitTellModule(FileManager fileManager,
                            FPlayerService fPlayerService,
                            ProxyConnector proxyConnector,
                            IntegrationModule integrationModule,
                            BukkitCommandUtil commandUtil) {
        super(fileManager, fPlayerService, proxyConnector, integrationModule, commandUtil);

        this.commandUtil = commandUtil;
    }

    @Override
    public void createCommand() {
        String promptPlayer = getPrompt().getPlayer();
        String promptMessage = getPrompt().getMessage();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new StringArgument(promptPlayer)
                        .includeSuggestions(commandUtil.argumentFPlayers(getCommand().isSuggestOfflinePlayers()))
                        .then(new GreedyStringArgument(promptMessage)
                                .executes(this::executesFPlayer))
                )
                .override();
    }
}
