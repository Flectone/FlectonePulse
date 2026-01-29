package net.flectone.pulse.module.message.format.scoreboard;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.scoreboard.listener.ScoreboardPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;

public abstract class ScoreboardModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;

    protected ScoreboardModule(FileFacade fileFacade,
                               ListenerRegistry listenerRegistry) {
        this.fileFacade = fileFacade;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public Message.Format.Scoreboard config() {
        return fileFacade.message().format().scoreboard();
    }

    @Override
    public Permission.Message.Format.Scoreboard permission() {
        return fileFacade.permission().message().format().scoreboard();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(ScoreboardPulseListener.class);
    }

    public abstract void create(FPlayer fPlayer, boolean skipCacheTeam);

    public abstract void remove(FPlayer fPlayer);

}
