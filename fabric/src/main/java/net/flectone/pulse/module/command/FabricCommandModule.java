package net.flectone.pulse.module.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;

@Singleton
public class FabricCommandModule extends CommandModule {

    @Inject
    public FabricCommandModule(FileManager fileManager) {
        super(fileManager);
    }

    @Override
    public void reload() {

    }
}
