package net.flectone.pulse.module.message.anvil;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class FabricAnvilModule extends AnvilModule {

    @Inject
    public FabricAnvilModule(FileResolver fileManager) {
        super(fileManager);
    }

    @Override
    public boolean format(FPlayer fPlayer, Object itemMeta) {
        return false;
    }
}
