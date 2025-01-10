package net.flectone.pulse.module.command.stream;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.util.CommandUtil;

@Singleton
public class FabricStreamModule extends StreamModule {

    @Inject
    public FabricStreamModule(FileManager fileManager,
                              ThreadManager threadManager,
                              CommandUtil commandUtil) {
        super(fileManager, threadManager, commandUtil);
    }

    @Override
    public void createCommand() {

    }
}
