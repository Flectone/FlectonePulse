package net.flectone.pulse.module.command.clearmail;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.IStringTooltip;
import dev.jorel.commandapi.StringTooltip;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.CommandUtil;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

@Singleton
public class BukkitClearmailModule extends ClearmailModule {

    private final FPlayerManager fPlayerManager;
    private final Database database;
    private final FLogger fLogger;

    @Inject
    public BukkitClearmailModule(FileManager fileManager,
                                 CommandUtil commandUtil,
                                 FPlayerManager fPlayerManager,
                                 Database database,
                                 FLogger fLogger) {
        super(fileManager, commandUtil);

        this.fPlayerManager = fPlayerManager;
        this.database = database;
        this.fLogger = fLogger;
    }

    @Override
    public void createCommand() {
        String name = getCommand().getAliases().get(0);
        String prompt = getPrompt().getId();

        new FCommand(name)
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new IntegerArgument(prompt)
                        .replaceSuggestions(ArgumentSuggestions.stringsWithTooltipsAsync(info -> CompletableFuture.supplyAsync(() -> {
                            if (!(info.sender() instanceof Player player)) return new IStringTooltip[]{};

                            FPlayer fPlayer = fPlayerManager.get(player);

                            try {
                                return database.getMails(fPlayer)
                                        .stream()
                                        .map(mail -> StringTooltip.ofString(String.valueOf(mail.id()), mail.message()))
                                        .toList()
                                        .toArray(new IStringTooltip[]{});
                            } catch (SQLException e) {
                                fLogger.warning(e);
                                return new IStringTooltip[]{};
                            }
                        })))
                        .executes(this::executesFPlayerDatabase)
                )
                .override();
    }
}
