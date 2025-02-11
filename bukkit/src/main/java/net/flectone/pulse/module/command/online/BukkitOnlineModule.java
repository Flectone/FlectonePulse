package net.flectone.pulse.module.command.online;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.BukkitCommandUtil;
import net.flectone.pulse.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;

import java.util.function.BiFunction;

@Singleton
public class BukkitOnlineModule extends OnlineModule {

    private final TimeUtil timeUtil;
    private final BukkitCommandUtil commandUtil;
    private final IntegrationModule integrationModule;

    @Inject
    public BukkitOnlineModule(FileManager fileManager,
                              FPlayerDAO fPlayerDAO,
                              BukkitCommandUtil commandUtil,
                              TimeUtil timeUtil,
                              IntegrationModule integrationModule) {
        super(fileManager, fPlayerDAO, commandUtil);

        this.timeUtil = timeUtil;
        this.commandUtil = commandUtil;
        this.integrationModule = integrationModule;
    }

    @Override
    public BiFunction<FPlayer, Localization.Command.Online, String> getResolver(FPlayer fPlayer, FPlayer targetFPlayer, String argument) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetFPlayer.getUuid());

        return (fResolver, s) -> switch (argument) {
            case "first" -> timeUtil.format(
                    fPlayer,
                    System.currentTimeMillis() - offlinePlayer.getFirstPlayed(),
                    s.getFormatFirst()
            );
            case "last" -> offlinePlayer.isOnline() && !integrationModule.isVanished(targetFPlayer)
                    ? s.getFormatCurrent()
                    : timeUtil.format(fPlayer, System.currentTimeMillis() - offlinePlayer.getLastPlayed(),
                    s.getFormatLast()
            );
            case "total" -> timeUtil.format(fPlayer,
                    offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE),
                    s.getFormatTotal()
            );
            default -> "";
        };
    }

    @Override
    public void createCommand() {
        String prompt = getPrompt().getPlayer();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new MultiLiteralArgument("type", "first", "last", "total")
                        .then(new StringArgument(prompt)
                                .includeSuggestions(commandUtil.argumentFPlayers(getCommand().isSuggestOfflinePlayers()))
                                .executes(this::executesFPlayer)
                        )
                )
                .override();
    }
}
