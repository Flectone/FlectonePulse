package net.flectone.pulse.module.command.stream;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.CommandUtil;

@Singleton
public class BukkitStreamModule extends StreamModule {

    @Inject
    public BukkitStreamModule(FileManager fileManager, ThreadManager threadManager, CommandUtil commandUtil) {
        super(fileManager, threadManager, commandUtil);
    }

    @Override
    public void createCommand() {
        String prompt = getPrompt().getUrl();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new LiteralArgument("end")
                        .executesPlayer(this::executesFPlayer))
                .then(new LiteralArgument("start")
                        .then(new GreedyStringArgument(prompt)
                                .setOptional(true)
                                .executes(this::executesFPlayer)))
                .override();
    }
}
