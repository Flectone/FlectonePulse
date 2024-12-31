package net.flectone.pulse.module.command.poll;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.*;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ProxyManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.ComponentUtil;

@Singleton
public class BukkitPollModule extends PollModule {

    private final FPlayerManager fPlayerManager;

    @Inject
    public BukkitPollModule(FileManager fileManager,
                            ProxyManager proxyManager,
                            ThreadManager threadManager,
                            FPlayerManager fPlayerManager,
                            CommandUtil commandUtil,
                            ComponentUtil componentUtil,
                            Gson gson) {
        super(fileManager, proxyManager, threadManager, commandUtil, componentUtil, gson);

        this.fPlayerManager = fPlayerManager;
    }

    @Override
    public void createCommand() {
        String name = getCommand().getAliases().get(0);
        String promptMessage = getPrompt().getMessage();
        String promptTime = getPrompt().getTime();
        String promptMultipleVote = getPrompt().getMultipleVote();
        String promptId = getPrompt().getId();
        String promptNumber = getPrompt().getNumber();

        new FCommand(name)
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new LiteralArgument("create")
                        .withPermission(getPermission().getCreate().getName())
                        .then(new IntegerArgument(promptTime, 1, getCommand().getMaxTime())
                                .then(new BooleanArgument(promptMultipleVote)
                                        .then(new TextArgument("title")
                                                .then(new MapArgumentBuilder<String, String>(promptMessage)
                                                        .withKeyMapper(s -> s)
                                                        .withValueMapper(s -> s)
                                                        .withoutKeyList()
                                                        .withoutValueList()
                                                        .build()
                                                        .executes((commandSender, commandArguments) -> {
                                                            onCommandCreate(fPlayerManager.convert(commandSender), commandArguments);
                                                        })
                                                )
                                        )
                                )
                        )
                )
                .then(new LiteralArgument("vote").setListed(false)
                        .then(new IntegerArgument(promptId)
                                .then(new IntegerArgument(promptNumber)
                                        .executesPlayer(this::executesFPlayer)
                                )
                        )
                )
                .override();
    }
}
