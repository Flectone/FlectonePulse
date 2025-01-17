package net.flectone.pulse.module.command.coin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.RandomUtil;

@Singleton
public class BukkitCoinModule extends CoinModule {

    @Inject
    public BukkitCoinModule(FileManager fileManager,
                            RandomUtil randomUtil) {
        super(fileManager, randomUtil);
    }

    @Override
    public void createCommand() {
        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .executes(this::executesFPlayer)
                .override();
    }
}
