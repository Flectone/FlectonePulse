package net.flectone.pulse.module.command.spit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Singleton
public class BukkitSpitModule extends SpitModule {

    @Inject
    public BukkitSpitModule(FileManager fileManager,
                            net.flectone.pulse.module.message.contact.spit.SpitModule spitModule,
                            CommandUtil commandUtil) {
        super(fileManager, fPlayer -> {
            Player player = Bukkit.getPlayer(fPlayer.getUuid());
            if (player == null) return;

            spitModule.send(fPlayer, player.getLocation());
        }, commandUtil);
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
