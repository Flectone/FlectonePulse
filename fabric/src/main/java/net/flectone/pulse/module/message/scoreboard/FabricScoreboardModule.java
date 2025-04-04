package net.flectone.pulse.module.message.sidebar;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;

@Singleton
public class FabricScoreboardModule extends ScoreboardModule {

    @Inject
    public FabricScoreboardModule(FileManager fileManager) {
        super(fileManager);
    }

    @Override
    public void send(FPlayer fPlayer) {

    }
}
