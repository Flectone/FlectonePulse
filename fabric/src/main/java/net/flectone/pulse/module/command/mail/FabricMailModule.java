package net.flectone.pulse.module.command.mail;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.CommandUtil;

@Singleton
public class FabricMailModule extends MailModule {

    @Inject
    public FabricMailModule(FileManager fileManager,
                            TellModule tellModule,
                            ThreadManager threadManager,
                            IntegrationModule integrationModule,
                            CommandUtil commandUtil) {
        super(fileManager, tellModule, threadManager, integrationModule, commandUtil);
    }

    @Override
    public void createCommand() {

    }
}
