package net.flectone.pulse.module.command.tell;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ProxyManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.CommandUtil;

@Singleton
public class FabricTellModule extends TellModule {

    @Inject
    public FabricTellModule(FileManager fileManager,
                            ThreadManager threadManager,
                            FPlayerManager fPlayerManager,
                            ProxyManager proxyManager,
                            IntegrationModule integrationModule,
                            CommandUtil commandUtil) {
        super(fileManager, threadManager, fPlayerManager, proxyManager, integrationModule, commandUtil);
    }

    @Override
    public void createCommand() {

    }
}
