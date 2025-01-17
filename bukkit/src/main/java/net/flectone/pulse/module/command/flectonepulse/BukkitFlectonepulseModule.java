package net.flectone.pulse.module.command.flectonepulse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.TimeUtil;

@Singleton
public class BukkitFlectonepulseModule extends FlectonepulseModule {

    @Inject
    public BukkitFlectonepulseModule(FileManager fileManager,
                                     TimeUtil timeUtil,
                                     FlectonePulse flectonePulse,
                                     FLogger fLogger) {
        super(fileManager, timeUtil, flectonePulse, fLogger);
    }

    @Override
    public void createCommand() {
        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new LiteralArgument("reload")
                        .then(new MultiLiteralArgument("type", "all", "text")
                                .executes(this::executesFPlayer)
                        )
                        .executes(this::executesFPlayer)
                )
                .override();
    }
}
