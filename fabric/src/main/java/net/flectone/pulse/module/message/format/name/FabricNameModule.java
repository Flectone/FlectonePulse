package net.flectone.pulse.module.message.format.name;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;

@Singleton
public class FabricNameModule extends NameModule {

    @Inject
    public FabricNameModule(FileManager fileManager,
                            IntegrationModule integrationModule) {
        super(fileManager, integrationModule);
    }

    @Override
    public void add(FPlayer fPlayer) {

    }

    @Override
    public void remove(FPlayer fPlayer) {

    }
}
