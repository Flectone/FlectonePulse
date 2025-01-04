package net.flectone.pulse.module.command.broadcast;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.CommandUtil;

@Singleton
public class BukkitBroadcastModule extends BroadcastModule {

    @Inject
    public BukkitBroadcastModule(FileManager fileManager,
                                 CommandUtil commandUtil) {
        super(fileManager, commandUtil);
    }

    @Override
    public void createCommand() {
        String name = getCommand().getAliases().get(0);
        String prompt = getPrompt().getMessage();

        new FCommand(name)
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new GreedyStringArgument(prompt)
                        .executes(this::executesFPlayer)
                )
                .override();
    }
}
