package net.flectone.pulse.module.message.scoreboard;

import com.google.common.collect.ImmutableSet;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.message.scoreboard.listener.PulseScoreboardListener;
import net.flectone.pulse.module.message.scoreboard.objective.ObjectiveModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

public abstract class ScoreboardModule implements ModuleLocalization<Localization.Message.Scoreboard> {

    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final SocialService socialService;

    protected ScoreboardModule(FileFacade fileFacade,
                               ListenerRegistry listenerRegistry,
                               PlatformPlayerAdapter platformPlayerAdapter,
                               SocialService socialService) {
        this.fileFacade = fileFacade;
        this.listenerRegistry = listenerRegistry;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.socialService = socialService;
    }

    @Override
    public ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        return ModuleLocalization.super.childrenBuilder().add(ObjectiveModule.class);
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_SCOREBOARD;
    }

    @Override
    public Message.Scoreboard config() {
        return fileFacade.message().scoreboard();
    }

    @Override
    public Permission.Message.Scoreboard permission() {
        return fileFacade.permission().message().scoreboard();
    }

    @Override
    public Localization.Message.Scoreboard localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().scoreboard();
    }

    @Override
    public void onEnable() {
        listenerRegistry.register(PulseScoreboardListener.class);
    }

    protected boolean isInvisibleNameFor(@NonNull FPlayer fPlayer) {
        return !config().nameVisible() || config().hideNameWhenSneaking() && platformPlayerAdapter.isSneaking(fPlayer);
    }

    public abstract void createOrUpdate(@NonNull FPlayer fPlayer);

    public abstract void remove(@NonNull FPlayer fPlayer);

}
