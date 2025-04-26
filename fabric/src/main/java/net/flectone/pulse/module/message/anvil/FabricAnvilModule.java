package net.flectone.pulse.module.message.anvil;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;

@Singleton
public class FabricAnvilModule extends AnvilModule {

    @Inject
    public FabricAnvilModule(FileManager fileManager) {
        super(fileManager);
    }

    @Override
    public boolean format(FPlayer fPlayer, Object itemMeta) {
        return false;
    }
}
