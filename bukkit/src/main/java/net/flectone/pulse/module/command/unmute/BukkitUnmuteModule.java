package net.flectone.pulse.module.command.unmute;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ProxyManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.CommandUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Singleton
public class BukkitUnmuteModule extends UnmuteModule {

    private final Database database;
    private final FLogger fLogger;

    @Inject
    public BukkitUnmuteModule(FileManager fileManager,
                              ThreadManager threadManager,
                              ProxyManager proxyManager,
                              Database database,
                              FLogger fLogger,
                              CommandUtil commandUtil,
                              Gson gson) {
        super(fileManager, threadManager, proxyManager, commandUtil, gson);

        this.database = database;
        this.fLogger = fLogger;
    }

    @Override
    public void createCommand() {
        String name = getCommand().getAliases().get(0);
        String prompt = getPrompt().getPlayer();
        String promptNumber = getPrompt().getNumber();

        new FCommand(name)
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new StringArgument(prompt)
                        .includeSuggestions(ArgumentSuggestions.stringCollectionAsync(info -> CompletableFuture.supplyAsync(() -> {
                            try {
                                return database.getValidModerationsNames(Moderation.Type.MUTE);
                            } catch (SQLException e) {
                                fLogger.warning(e);
                            }

                            return new ArrayList<>();
                        })))
                        .then(new IntegerArgument(promptNumber).setOptional(true)
                                .executes(this::executesFPlayer)
                        )
                )
                .override();
    }
}
