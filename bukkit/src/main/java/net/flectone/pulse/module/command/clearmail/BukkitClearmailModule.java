package net.flectone.pulse.module.command.clearmail;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.IStringTooltip;
import dev.jorel.commandapi.StringTooltip;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.CommandUtil;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

@Singleton
public class BukkitClearmailModule extends ClearmailModule {

    private final FPlayerService fPlayerService;

    @Inject
    public BukkitClearmailModule(FileManager fileManager,
                                 FPlayerService fPlayerService,
                                 CommandUtil commandUtil) {
        super(fileManager, fPlayerService, commandUtil);

        this.fPlayerService = fPlayerService;
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

                            FPlayer fPlayer = fPlayerService.getFPlayer(player.getUniqueId());

                            return fPlayerService.getMails(fPlayer)
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
