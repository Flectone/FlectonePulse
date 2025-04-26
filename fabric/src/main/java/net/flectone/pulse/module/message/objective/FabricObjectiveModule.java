package net.flectone.pulse.module.message.objective;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;

@Singleton
public class FabricObjectiveModule extends ObjectiveModule {

    @Inject
    public FabricObjectiveModule(FileManager fileManager) {
        super(fileManager);
    }
}
