package net.flectone.pulse.module.command.mute;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.BukkitCommandUtil;
import net.flectone.pulse.util.TimeUtil;

import java.sql.SQLException;
import java.util.List;

@Singleton
public class BukkitMuteModule extends MuteModule {

    private final Database database;
    private final BukkitCommandUtil commandUtil;
    private final TimeUtil timeUtil;

    @Inject
    public BukkitMuteModule(FileManager fileManager,
                            ThreadManager threadManager,
                            FPlayerManager fPlayerManager,
                            Database database,
                            BukkitCommandUtil commandUtil,
                            TimeUtil timeUtil,
                            Gson gson) {
        super(fileManager, threadManager, fPlayerManager, commandUtil, timeUtil, gson);

        this.database = database;
        this.commandUtil = commandUtil;
        this.timeUtil = timeUtil;
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

    @Override
    public void sendForTarget(FEntity fPlayer, FPlayer fTarget, Moderation mute) throws SQLException {
        if (checkModulePredicates(fPlayer)) return;

        List<Moderation> mutes = database.getModerations(fTarget, Moderation.Type.MUTE);
        if (mutes.isEmpty()) return;

        builder(fPlayer)
                .receiver(fTarget)
                .format(s -> timeUtil.format(fTarget, mute.getRemainingTime(), s.getPlayer()
                                .replace("<message>", s.getReasons().getConstant(mute.getReason()))
                                .replace("<moderator>", fPlayer.getName())
                        )
                )
                .sound(getSound())
                .sendBuilt();
    }
}
