package net.flectone.pulse.module.command.flectonepulse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.module.command.FCommand;

@Singleton
public class BukkitFlectonepulseModule extends FlectonepulseModule {

    @Inject
    public BukkitFlectonepulseModule(FileManager fileManager,
                                     ThreadManager threadManager,
                                     FlectonePulse flectonePulse,
                                     FLogger fLogger) {
        super(fileManager, threadManager, flectonePulse, fLogger);
    }

    @Override
    public void createCommand() {
        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new LiteralArgument("reload")
                        .executes(this::executesFPlayer)
                )
                .override();
    }
}
