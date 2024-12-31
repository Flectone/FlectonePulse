package net.flectone.pulse.module.command.try_;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.RandomUtil;

@Singleton
public class BukkitTryModule extends TryModule {

    @Inject
    public BukkitTryModule(FileManager fileManager,
                           RandomUtil randomUtil,
                           CommandUtil commandUtil) {
        super(fileManager, randomUtil, commandUtil);
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
