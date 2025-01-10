package net.flectone.pulse.module.message.bubble;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import org.jetbrains.annotations.NotNull;

@Singleton
public class FabricBubbleModule extends BubbleModule {

    @Inject
    public FabricBubbleModule(FileManager fileManager) {
        super(fileManager);
    }

    @Override
    public void add(@NotNull FPlayer fPlayer, @NotNull String inputString) {

    }
}
