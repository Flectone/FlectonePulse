package net.flectone.pulse.platform.handler;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;

import java.io.IOException;
import java.util.UUID;

@Singleton
public class MinecraftProxyMessageHandler extends ProxyMessageHandler {

    private final Injector injector;

    @Inject
    public MinecraftProxyMessageHandler(Injector injector,
                                        FileFacade fileFacade,
                                        FPlayerService fPlayerService,
                                        FLogger fLogger,
                                        ModerationService moderationService,
                                        Gson gson,
                                        TaskScheduler taskScheduler) {
        super(injector, fileFacade, fPlayerService, fLogger, moderationService, gson, taskScheduler);

        this.injector = injector;
    }

    @Override
    public void handleSystemOnline(UUID uuid) throws IOException {
        super.handleSystemOnline(uuid);

        injector.getInstance(PlayerlistnameModule.class).add(uuid);
    }

    @Override
    public void handleSystemOffline(UUID uuid) throws IOException {
        super.handleSystemOffline(uuid);

        injector.getInstance(PlayerlistnameModule.class).remove(uuid);
    }

}
