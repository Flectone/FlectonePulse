package net.flectone.pulse.module.command.ball;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.RandomUtil;

@Singleton
public class BukkitBallModule extends BallModule {

    @Inject
    public BukkitBallModule(FileManager fileManager, RandomUtil randomUtil, CommandUtil commandUtil) {
        super(fileManager, randomUtil, commandUtil);
    }

    @Override
    public void createCommand() {
        String prompt = getPrompt().getMessage();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new GreedyStringArgument(prompt)
                        .executes(this::executesFPlayer)
                )
                .override();
    }
}
