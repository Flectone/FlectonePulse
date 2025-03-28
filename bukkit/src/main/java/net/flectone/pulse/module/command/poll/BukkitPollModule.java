package net.flectone.pulse.module.command.poll;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.*;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.connector.ProxyConnector;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.BukkitCommandUtil;
import net.flectone.pulse.util.ComponentUtil;

@Singleton
public class BukkitPollModule extends PollModule {

    private final FPlayerService fPlayerService;
    private final BukkitCommandUtil commandUtil;

    @Inject
    public BukkitPollModule(FileManager fileManager,
                            ProxyConnector proxyConnector,
                            TaskScheduler taskScheduler,
                            FPlayerService fPlayerService,
                            BukkitCommandUtil commandUtil,
                            ComponentUtil componentUtil,
                            Gson gson) {
        super(fileManager, proxyConnector, taskScheduler, commandUtil, componentUtil, gson);

        this.fPlayerService = fPlayerService;
        this.commandUtil = commandUtil;
    }

    @Override
    public void createCommand() {
        String promptMessage = getPrompt().getMessage();
        String promptTime = getPrompt().getTime();
        String promptRepeatTime = getPrompt().getRepeatTime();
        String promptMultipleVote = getPrompt().getMultipleVote();
        String promptId = getPrompt().getId();
        String promptNumber = getPrompt().getNumber();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new LiteralArgument("create")
                        .withPermission(getPermission().getCreate().getName())
                        .then(commandUtil.timeArgument(promptTime)
                                .then(commandUtil.timeArgument(promptRepeatTime)
                                        .then(new BooleanArgument(promptMultipleVote)
                                                .then(new TextArgument("title")
                                                        .then(new MapArgumentBuilder<String, String>(promptMessage)
                                                                .withKeyMapper(s -> s)
                                                                .withValueMapper(s -> s)
                                                                .withoutKeyList()
                                                                .withoutValueList()
                                                                .build()
                                                                .executes((commandSender, commandArguments) -> {
                                                                    onCommandCreate(fPlayerService.getFPlayer(commandSender), commandArguments);
                                                                })
                                                        )
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
