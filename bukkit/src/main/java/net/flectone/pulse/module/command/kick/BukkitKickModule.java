package net.flectone.pulse.module.command.kick;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.BukkitCommandUtil;
import net.flectone.pulse.util.ComponentUtil;

@Singleton
public class BukkitKickModule extends KickModule {

    private final BukkitCommandUtil commandUtil;

    @Inject
    public BukkitKickModule(FileManager fileManager,
                            FPlayerManager fPlayerManager,
                            BukkitCommandUtil commandUtil,
                            ComponentUtil componentUtil,
                            Gson gson) {
        super(fileManager, fPlayerManager, commandUtil, componentUtil, gson);

        this.commandUtil = commandUtil;
    }

    @Override
    public void createCommand() {
        String name = getCommand().getAliases().get(0);
        String playerPrompt = getPrompt().getPlayer();
        String messagePrompt = getPrompt().getMessage();

        new FCommand(name)
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new StringArgument(playerPrompt)
                        .includeSuggestions(commandUtil.argumentFPlayers(false))
                        .then(new GreedyStringArgument(messagePrompt).setOptional(true)
                                .executes(this::executesFPlayerDatabase)))
                .override();
    }
}
