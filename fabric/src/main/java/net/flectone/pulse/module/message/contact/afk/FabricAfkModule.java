package net.flectone.pulse.module.message.contact.afk;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FPlayer;
import org.jetbrains.annotations.NotNull;

@Singleton
public class FabricAfkModule extends AfkModule {

    @Inject
    public FabricAfkModule(FileManager fileManager, ThreadManager threadManager) {
        super(fileManager, threadManager);
    }

    @Override
    public void remove(@NotNull String action, FPlayer fPlayer) {

    }

    @Override
    public void check(@NotNull FPlayer fPlayer) {

    }
}
