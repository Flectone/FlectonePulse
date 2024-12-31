package net.flectone.pulse.module.command.spit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import org.bukkit.Bukkit;

@Singleton
public class BukkitSpitModule extends SpitModule {

    @Inject
    public BukkitSpitModule(FileManager fileManager,
                            net.flectone.pulse.module.message.contact.spit.SpitModule spitModule) {
        super(fileManager, fPlayer -> spitModule.send(fPlayer, Bukkit.getPlayer(fPlayer.getUuid()).getLocation()));
    }

    @Override
    public void createCommand() {
        String name = getCommand().getAliases().get(0);

        new FCommand(name)
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .executesPlayer(this::executesFPlayer)
                .override();
    }
}
