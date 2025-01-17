package net.flectone.pulse.module.command.afk;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;

@Singleton
public class BukkitAfkModule extends AfkModule {

    @Inject
    public BukkitAfkModule(FileManager fileManager, net.flectone.pulse.module.message.contact.afk.AfkModule afkModule) {
        super(fileManager, afkModule);
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
