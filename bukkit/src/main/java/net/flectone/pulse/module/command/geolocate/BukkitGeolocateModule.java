package net.flectone.pulse.module.command.geolocate;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.BukkitCommandUtil;

@Singleton
public class BukkitGeolocateModule extends GeolocateModule {

    private final BukkitCommandUtil commandManager;

    @Inject
    public BukkitGeolocateModule(FileManager fileManager,
                                 BukkitCommandUtil commandManager) {
        super(fileManager, commandManager);

        this.commandManager = commandManager;
    }

    @Override
    public void createCommand() {
        String name = getCommand().getAliases().get(0);
        String prompt = getPrompt().getPlayer();

        new FCommand(name)
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new StringArgument(prompt)
                        .includeSuggestions(commandManager.argumentFPlayers(getCommand().isSuggestOfflinePlayers()))
                        .executes(this::executesFPlayerDatabase)
                )
                .override();
    }
}
