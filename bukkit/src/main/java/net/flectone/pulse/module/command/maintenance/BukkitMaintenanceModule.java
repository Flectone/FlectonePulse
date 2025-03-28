package net.flectone.pulse.module.command.maintenance;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import dev.jorel.commandapi.arguments.BooleanArgument;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.*;

import java.nio.file.Path;

@Singleton
public class BukkitMaintenanceModule extends MaintenanceModule {

    private final FileManager fileManager;

    @Inject
    public BukkitMaintenanceModule(FileManager fileManager,
                                   FPlayerService fPlayerService,
                                   PermissionUtil permissionUtil,
                                   ListenerRegistry listenerRegistry,
                                   @Named("projectPath") Path projectPath,
                                   FileUtil fileUtil,
                                   CommandUtil commandUtil,
                                   ComponentUtil componentUtil,
                                   PacketEventsUtil packetEventsUtil) {
        super(fileManager, fPlayerService, permissionUtil, listenerRegistry, projectPath, fileUtil, commandUtil, componentUtil, packetEventsUtil);

        this.fileManager = fileManager;
    }

    @Override
    public void createCommand() {
        String prompt = fileManager.getLocalization().getCommand().getPrompt().getTurn();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new BooleanArgument(prompt)
                        .executes(this::executesFPlayer)
                )
                .override();
    }
}
