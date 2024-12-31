package net.flectone.pulse.module.command.dice;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.IntegerArgument;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.RandomUtil;

@Singleton
public class BukkitDiceModule extends DiceModule {

    @Inject
    public BukkitDiceModule(FileManager fileManager,
                            CommandUtil commandUtil,
                            RandomUtil randomUtil,
                            Gson gson) {
        super(fileManager, commandUtil, randomUtil, gson);
    }

    @Override
    public void createCommand() {
        String name = getCommand().getAliases().get(0);
        String prompt = getPrompt().getNumber();

        new FCommand(name)
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new IntegerArgument(prompt, getCommand().getMin(), getCommand().getMax())
                        .setOptional(true)
                        .executes(this::executesFPlayer)
                )
                .override();
    }
}
