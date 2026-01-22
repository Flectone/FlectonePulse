package net.flectone.pulse.module.message.bubble;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

import java.util.List;

public abstract class BubbleModule extends AbstractModule {

    private final FileFacade fileFacade;

    protected BubbleModule(FileFacade fileFacade) {
        this.fileFacade = fileFacade;
    }

    @Override
    public Message.Bubble config() {
        return fileFacade.message().bubble();
    }

    @Override
    public Permission.Message.Bubble permission() {
        return fileFacade.permission().message().bubble();
    }

    public abstract void add(@NonNull FPlayer fPlayer, @NonNull String inputString, List<FPlayer> receivers);

    public enum Billboard {

        FIXED,
        VERTICAL,
        HORIZONTAL,
        CENTER

    }
}
