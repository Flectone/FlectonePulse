package net.flectone.pulse.module.command.warn;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.BukkitCommandUtil;
import net.flectone.pulse.util.TimeUtil;

@Singleton
public class BukkitWarnModule extends WarnModule {

    private final BukkitCommandUtil commandUtil;

    @Inject
    public BukkitWarnModule(FileManager fileManager,
                            ThreadManager threadManager,
                            TimeUtil timeUtil,
                            BukkitCommandUtil commandUtil,
                            Gson gson) {
        super(fileManager, threadManager, timeUtil, commandUtil, gson);

        this.commandUtil = commandUtil;
    }

    @Override
    public void createCommand() {
        String name = getCommand().getAliases().get(0);
        String promptPlayer = getPrompt().getPlayer();
        String promptReason = getPrompt().getReason();
        String promptTime = getPrompt().getTime();

        new FCommand(name)
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new StringArgument(promptPlayer)
                        .includeSuggestions(commandUtil.argumentFPlayers(getCommand().isSuggestOfflinePlayers()))
                        .then(commandUtil.timeArgument(promptTime)
                                .then(new GreedyStringArgument(promptReason).setOptional(true)
                                        .executes(this::executesFPlayer)
                                )
                        )
                )
                .override();
    }
}
