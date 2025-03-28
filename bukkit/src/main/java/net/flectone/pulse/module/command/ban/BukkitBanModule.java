package net.flectone.pulse.module.command.ban;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.*;

@Singleton
public class BukkitBanModule extends BanModule {

    private final BukkitCommandUtil commandManager;

    @Inject
    public BukkitBanModule(FileManager fileManager,
                           FPlayerService fPlayerService,
                           ModerationService moderationService,
                           ModerationUtil moderationUtil,
                           PermissionUtil permissionUtil,
                           BukkitCommandUtil commandManager,
                           ComponentUtil componentUtil,
                           PacketEventsUtil packetEventsUtil,
                           Gson gson) {
        super(fileManager, fPlayerService, moderationService, moderationUtil, permissionUtil, commandManager, componentUtil,
                packetEventsUtil, gson);

        this.commandManager = commandManager;
    }

    @Override
    public void createCommand() {
        String promptPlayer = getPrompt().getPlayer();
        String promptReason = getPrompt().getReason();
        String promptTime = getPrompt().getTime();

        new FCommand(getName(getCommand()))
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
