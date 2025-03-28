package net.flectone.pulse.module.command.kick;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.BukkitCommandUtil;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.ModerationUtil;

@Singleton
public class BukkitKickModule extends KickModule {

    private final BukkitCommandUtil commandUtil;

    @Inject
    public BukkitKickModule(FileManager fileManager,
                            FPlayerService fPlayerService,
                            ModerationService moderationService,
                            ModerationUtil moderationUtil,
                            BukkitCommandUtil commandUtil,
                            ComponentUtil componentUtil,
                            Gson gson) {
        super(fileManager, fPlayerService, moderationService, moderationUtil, commandUtil, componentUtil, gson);

        this.commandUtil = commandUtil;
    }

    @Override
    public void createCommand() {
        String playerPrompt = getPrompt().getPlayer();
        String messagePrompt = getPrompt().getMessage();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new StringArgument(playerPrompt)
                        .includeSuggestions(commandUtil.argumentFPlayers(false))
                        .then(new GreedyStringArgument(messagePrompt).setOptional(true)
                                .executes(this::executesFPlayer)))
                .override();
    }
}
