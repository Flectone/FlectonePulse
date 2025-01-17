package net.flectone.pulse.module.command.mute;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.BukkitCommandUtil;
import net.flectone.pulse.util.ModerationUtil;

@Singleton
public class BukkitMuteModule extends MuteModule {

    private final BukkitCommandUtil commandUtil;

    @Inject
    public BukkitMuteModule(FileManager fileManager,
                            ThreadManager threadManager,
                            FPlayerManager fPlayerManager,
                            BukkitCommandUtil commandUtil,
                            ModerationUtil moderationUtil,
                            Gson gson) {
        super(fileManager, threadManager, fPlayerManager, commandUtil, moderationUtil, gson);

        this.commandUtil = commandUtil;
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
