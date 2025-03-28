package net.flectone.pulse.module.command.unban;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.CommandUtil;

import java.util.concurrent.CompletableFuture;

@Singleton
public class BukkitUnbanModule extends UnbanModule {

    private final ModerationService moderationService;

    @Inject
    public BukkitUnbanModule(FileManager fileManager,
                             FPlayerService fPlayerService,
                             ModerationService moderationService,
                             CommandUtil commandUtil,
                             Gson gson) {
        super(fileManager, fPlayerService, moderationService, commandUtil, gson);

        this.moderationService = moderationService;
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
                                CompletableFuture.supplyAsync(() -> moderationService.getValidNames(Moderation.Type.BAN))))
                        .then(new IntegerArgument(promptNumber).setOptional(true)
                                .executes(this::executesFPlayer)
                        )
                )
                .override();
    }
}
