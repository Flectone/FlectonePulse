package net.flectone.pulse.module.command.ignore;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.BukkitCommandUtil;

@Singleton
public class BukkitIngoreModule extends IgnoreModule {

    private final BukkitCommandUtil commandUtil;

    @Inject
    public BukkitIngoreModule(FileManager fileManager,
                              FPlayerService fPlayerService,
                              BukkitCommandUtil commandUtil) {
        super(fileManager, fPlayerService, commandUtil);

        this.commandUtil = commandUtil;
    }

    @Override
    public void createCommand() {
        String prompt = getPrompt().getMessage();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new StringArgument(prompt)
                        .includeSuggestions(commandUtil.argumentFPlayers(getCommand().isSuggestOfflinePlayers()))
                        .executesPlayer(this::executesFPlayer)
                )
                .override();
    }
}
