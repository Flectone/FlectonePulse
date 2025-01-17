package net.flectone.pulse.module.command.helper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ProxyManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.PermissionUtil;

@Singleton
public class BukkitHelperModule extends HelperModule {

    @Inject
    public BukkitHelperModule(FileManager fileManager,
                              FPlayerManager fPlayerManager,
                              ProxyManager proxyManager,
                              PermissionUtil permissionUtil,
                              CommandUtil commandUtil) {
        super(fileManager, fPlayerManager, proxyManager, permissionUtil, commandUtil);
    }

    @Override
    public void createCommand() {
        String prompt = getPrompt().getMessage();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new GreedyStringArgument(prompt)
                        .executesPlayer(this::executesFPlayerDatabase)
                )
                .override();
    }
}
