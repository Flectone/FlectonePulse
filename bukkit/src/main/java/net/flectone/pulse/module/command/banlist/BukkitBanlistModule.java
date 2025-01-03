package net.flectone.pulse.module.command.banlist;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.platform.PlatformSender;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.TimeUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Singleton
public class BukkitBanlistModule extends BanlistModule {

    private final Database database;
    private final FLogger fLogger;

    @Inject
    public BukkitBanlistModule(FileManager fileManager,
                               CommandUtil commandUtil,
                               ComponentUtil componentUtil,
                               TimeUtil timeUtil,
                               PlatformSender platformSender,
                               Database database,
                               FLogger fLogger) {
        super(fileManager, commandUtil, componentUtil, timeUtil, platformSender);

        this.database = database;
        this.fLogger = fLogger;
    }

    @Override
    public void createCommand() {
        String name = getCommand().getAliases().get(0);
        String promptPlayer = getPrompt().getPlayer();
        String promptNumber = getPrompt().getNumber();

        new FCommand(name)
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new IntegerArgument(promptNumber).setOptional(true)
                        .executesPlayer(this::executesFPlayerDatabase)
                )
                .then(new StringArgument(promptPlayer)
                        .includeSuggestions(ArgumentSuggestions.stringCollectionAsync(info -> CompletableFuture.supplyAsync(() -> {

                            try {
                                return database.getModerationsNames(Moderation.Type.BAN);
                            } catch (SQLException e) {
                                fLogger.warning(e);
                            }

                            return new ArrayList<>();
                        })))
                        .then(new IntegerArgument(promptNumber).setOptional(true)
                                .executesPlayer(this::executesFPlayerDatabase)
                        )
                )
                .override();
    }
}
