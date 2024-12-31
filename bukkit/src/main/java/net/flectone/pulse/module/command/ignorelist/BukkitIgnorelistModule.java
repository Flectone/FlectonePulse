package net.flectone.pulse.module.command.ignorelist;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.IntegerArgument;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.platform.PlatformSender;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.TimeUtil;

@Singleton
public class BukkitIgnorelistModule extends IgnorelistModule {

    @Inject
    public BukkitIgnorelistModule(FileManager fileManager,
                                  PlatformSender platformSender,
                                  ComponentUtil componentUtil,
                                  CommandUtil commandUtil,
                                  TimeUtil timeUtil) {
        super(fileManager, platformSender, componentUtil, commandUtil, timeUtil);
    }

    @Override
    public void createCommand() {
        String name = getCommand().getAliases().get(0);
        String prompt = getPrompt().getNumber();

        new FCommand(name)
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new IntegerArgument(prompt).setOptional(true)
                        .executesPlayer(this::executesFPlayerDatabase)
                )
                .override();
    }
}
