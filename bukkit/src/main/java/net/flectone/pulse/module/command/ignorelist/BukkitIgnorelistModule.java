package net.flectone.pulse.module.command.ignorelist;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.IntegerArgument;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.platform.MessageSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.TimeUtil;

@Singleton
public class BukkitIgnorelistModule extends IgnorelistModule {

    @Inject
    public BukkitIgnorelistModule(FileManager fileManager,
                                  FPlayerService fPlayerService,
                                  MessageSender messageSender,
                                  ComponentUtil componentUtil,
                                  CommandUtil commandUtil,
                                  TimeUtil timeUtil) {
        super(fileManager, fPlayerService, messageSender, componentUtil, commandUtil, timeUtil);
    }

    @Override
    public void createCommand() {
        String prompt = getPrompt().getNumber();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new IntegerArgument(prompt).setOptional(true)
                        .executesPlayer(this::executesFPlayer)
                )
                .override();
    }
}
