package net.flectone.pulse.module.message.sidebar;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;

import java.util.List;

@Singleton
public class FabricSidebarModule extends SidebarModule {

    @Inject
    public FabricSidebarModule(FileManager fileManager, FPlayerService fPlayerService, TaskScheduler taskScheduler) {
        super(fileManager, fPlayerService, taskScheduler);
    }

    @Override
    public void send(FPlayer fPlayer) {

    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return List.of();
    }
}
