package net.flectone.pulse.module.message.format.scoreboard;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.message.format.scoreboard.listener.PulseScoreboardListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

public abstract class ScoreboardModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    protected ScoreboardModule(FileFacade fileFacade,
                               ListenerRegistry listenerRegistry,
                               PlatformPlayerAdapter platformPlayerAdapter) {
        this.fileFacade = fileFacade;
        this.listenerRegistry = listenerRegistry;
        this.platformPlayerAdapter = platformPlayerAdapter;
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_SCOREBOARD;
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
        listenerRegistry.register(PulseScoreboardListener.class);
    }

    public boolean isInvisibleNameFor(FPlayer fPlayer) {
        return !config().nameVisible() || config().hideNameWhenSneaking() && platformPlayerAdapter.isSneaking(fPlayer);
    }

    public abstract void create(FPlayer fPlayer, boolean skipCacheTeam);

    public abstract void remove(FPlayer fPlayer);

}
