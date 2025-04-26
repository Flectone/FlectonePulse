package net.flectone.pulse.module.message.format.scoreboard;

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
    public void add(FPlayer fPlayer) {

    }

    @Override
    public void remove(FPlayer fPlayer) {

    }
}
