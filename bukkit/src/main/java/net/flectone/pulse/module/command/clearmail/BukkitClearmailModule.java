package net.flectone.pulse.module.command.clearmail;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.IStringTooltip;
import dev.jorel.commandapi.StringTooltip;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.database.dao.MailDAO;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.CommandUtil;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

@Singleton
public class BukkitClearmailModule extends ClearmailModule {

    private final FPlayerManager fPlayerManager;
    private final MailDAO mailDAO;

    @Inject
    public BukkitClearmailModule(FileManager fileManager,
                                 FPlayerManager fPlayerManager,
                                 FPlayerDAO fPlayerDAO,
                                 MailDAO mailDAO,
                                 CommandUtil commandUtil) {
        super(fileManager, fPlayerDAO, mailDAO, commandUtil);

        this.fPlayerManager = fPlayerManager;
        this.mailDAO = mailDAO;
    }

    @Override
    public void createCommand() {
        String prompt = getPrompt().getId();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new IntegerArgument(prompt)
                        .replaceSuggestions(ArgumentSuggestions.stringsWithTooltipsAsync(info -> CompletableFuture.supplyAsync(() -> {
                            if (!(info.sender() instanceof Player player)) return new IStringTooltip[]{};

                            FPlayer fPlayer = fPlayerManager.get(player);

                            return mailDAO.get(fPlayer)
                                    .stream()
                                    .map(mail -> StringTooltip.ofString(String.valueOf(mail.id()), mail.message()))
                                    .toList()
                                    .toArray(new IStringTooltip[]{});
                        })))
                        .executes(this::executesFPlayer)
                )
                .override();
    }
}
