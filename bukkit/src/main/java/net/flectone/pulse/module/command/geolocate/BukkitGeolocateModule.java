package net.flectone.pulse.module.command.geolocate;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.BukkitCommandUtil;
import net.flectone.pulse.util.PacketEventsUtil;

@Singleton
public class BukkitGeolocateModule extends GeolocateModule {

    private final BukkitCommandUtil commandManager;

    @Inject
    public BukkitGeolocateModule(FileManager fileManager,
                                 FPlayerDAO fPlayerDAO,
                                 BukkitCommandUtil commandUtil,
                                 PacketEventsUtil packetEventsUtil) {
        super(fileManager, fPlayerDAO, commandUtil, packetEventsUtil);

        this.commandManager = commandUtil;
    }

    @Override
    public void createCommand() {
        String prompt = getPrompt().getPlayer();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new StringArgument(prompt)
                        .includeSuggestions(commandManager.argumentFPlayers(getCommand().isSuggestOfflinePlayers()))
                        .executes(this::executesFPlayer)
                )
                .override();
    }
}
