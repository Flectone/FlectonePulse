package net.flectone.pulse.module.command.unwarn;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.database.dao.ModerationDAO;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.CommandUtil;

import java.util.concurrent.CompletableFuture;

@Singleton
public class BukkitUnwarnModule extends UnwarnModule {

    private final ModerationDAO moderationDAO;

    @Inject
    public BukkitUnwarnModule(FileManager fileManager,
                              FPlayerDAO fPlayerDAO,
                              ModerationDAO moderationDAO,
                              CommandUtil commandUtil,
                              Gson gson) {
        super(fileManager, fPlayerDAO, moderationDAO, commandUtil, gson);

        this.moderationDAO = moderationDAO;
    }

    @Override
    public void createCommand() {
        String prompt = getPrompt().getPlayer();
        String promptNumber = getPrompt().getNumber();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new StringArgument(prompt)
                        .includeSuggestions(ArgumentSuggestions.stringCollectionAsync(info ->
                                CompletableFuture.supplyAsync(() ->
                                        moderationDAO.getValidPlayersNames(Moderation.Type.WARN))))
                        .then(new IntegerArgument(promptNumber).setOptional(true)
                                .executes(this::executesFPlayer)
                        )
                )
                .override();
    }
}
