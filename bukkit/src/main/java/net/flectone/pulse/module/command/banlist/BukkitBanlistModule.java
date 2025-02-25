package net.flectone.pulse.module.command.banlist;

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
import net.flectone.pulse.module.command.unban.UnbanModule;
import net.flectone.pulse.platform.MessageSender;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.ModerationUtil;

import java.util.concurrent.CompletableFuture;

@Singleton
public class BukkitBanlistModule extends BanlistModule {

    private final ModerationDAO moderationDAO;

    @Inject
    public BukkitBanlistModule(FileManager fileManager,
                               FPlayerDAO fPlayerDAO,
                               ModerationDAO moderationDAO,
                               UnbanModule unbanModule,
                               CommandUtil commandUtil,
                               ComponentUtil componentUtil,
                               ModerationUtil moderationUtil,
                               MessageSender messageSender) {
        super(fileManager, fPlayerDAO, moderationDAO, unbanModule, commandUtil, componentUtil, moderationUtil, messageSender);

        this.moderationDAO = moderationDAO;
    }

    @Override
    public void createCommand() {
        String promptPlayer = getPrompt().getPlayer();
        String promptNumber = getPrompt().getNumber();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new IntegerArgument(promptNumber).setOptional(true)
                        .executesPlayer(this::executesFPlayer)
                )
                .then(new StringArgument(promptPlayer)
                        .includeSuggestions(ArgumentSuggestions.stringCollectionAsync(info ->
                                CompletableFuture.supplyAsync(() ->
                                        moderationDAO.getPlayersNames(Moderation.Type.BAN))))
                        .then(new IntegerArgument(promptNumber).setOptional(true)
                                .executesPlayer(this::executesFPlayer)
                        )
                )
                .override();
    }
}
