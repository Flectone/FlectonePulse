package net.flectone.pulse.module.command.afk;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.CommandUtil;

@Singleton
public class BukkitAfkModule extends AfkModule {

    @Inject
    public BukkitAfkModule(FileManager fileManager,
                           net.flectone.pulse.module.message.afk.AfkModule afkModule,
                           CommandUtil commandUtil) {
        super(fileManager, afkModule, commandUtil);
    }

    @Override
    public void createCommand() {
        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .executesPlayer(this::executesFPlayer)
                .override();
    }

}
