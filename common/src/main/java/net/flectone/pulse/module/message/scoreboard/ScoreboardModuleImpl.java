package net.flectone.pulse.module.message.scoreboard;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.message.scoreboard.listener.PulseScoreboardListener;
import net.flectone.pulse.module.message.scoreboard.objective.ObjectiveModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.SocialService;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ScoreboardModuleImpl implements ScoreboardModule {

    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final SocialService socialService;

    @Override
    public Set<@NonNull Class<? extends ModuleSimple>> children() {
        Set<@NonNull Class<? extends ModuleSimple>> children = new LinkedHashSet<>(ScoreboardModule.super.children());
        children.add(ObjectiveModule.class);
        return Collections.unmodifiableSet(children);
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

}
