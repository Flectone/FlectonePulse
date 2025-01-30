package net.flectone.pulse.module.command.ping;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.BukkitCommandUtil;

@Singleton
public class BukkitPingModule extends PingModule {

    private final BukkitCommandUtil commandUtil;

    @Inject
    public BukkitPingModule(FileManager fileManager,
                            FPlayerManager fPlayerManager,
                            BukkitCommandUtil commandUtil,
                            IntegrationModule integrationModule) {
        super(fileManager, fPlayerManager, commandUtil, integrationModule);

        this.commandUtil = commandUtil;
    }

    @Override
    public void createCommand() {
        String prompt = getPrompt().getMessage();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new StringArgument(prompt)
                        .includeSuggestions(commandUtil.argumentFPlayers(false))
                        .executesPlayer(this::executesFPlayer)
                )
                .executes(this::executesFPlayer)
                .override();
    }
}
