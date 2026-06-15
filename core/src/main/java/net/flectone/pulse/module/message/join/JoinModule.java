package net.flectone.pulse.module.message.join;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.util.PlayTime;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.message.join.listener.JoinProxyMessageListener;
import net.flectone.pulse.module.message.join.listener.PulseJoinListener;
import net.flectone.pulse.module.message.join.model.JoinMetadata;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.proxy.RedisProxy;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.service.PlaytimeService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class JoinModule implements ModuleLocalization<Localization.Message.Join> {

    private final FileFacade fileFacade;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final PlaytimeService playtimeService;
    private final SocialService socialService;
    private final ProxyRegistry proxyRegistry;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        if (isProxyMode()) {
            listenerRegistry.register(JoinProxyMessageListener.class);
        }

        listenerRegistry.register(PulseJoinListener.class);
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_JOIN;
    }

    @Override
    public Message.Join config() {
        return fileFacade.message().join();
    }

    @Override
    public Permission.Message.Join permission() {
        return fileFacade.permission().message().join();
    }

    @Override
    public Localization.Message.Join localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().join();
    }

    public boolean isProxyMode() {
        return moduleController.isEnable(this) && config().range().type() == Range.Type.PROXY && proxyRegistry.hasEnabledProxy(proxy -> !(proxy instanceof RedisProxy));
    }

    public void send(FPlayer fPlayer, boolean fakeMessage) {
        send(fPlayer, fakeMessage, true);
    }

    public void send(FPlayer fPlayer, boolean fakeMessage, boolean checkProxyMode) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;
        if (checkProxyMode && isProxyMode()) return;

        PlayTime playTime = playtimeService.getPlayTime(fPlayer);
        boolean hasPlayedBefore = platformPlayerAdapter.hasPlayedBefore(fPlayer) || (playTime != null && playTime.sessions() > 1);
        boolean vanished = socialService.isVanished(fPlayer);
        EventMetadata.Builder<Localization.Message.Join> eventMetadataBuilder = EventMetadata.<Localization.Message.Join>builder()
                .sender(fPlayer)
                .format(localization -> hasPlayedBefore || !config().first() ? localization.format() : localization.formatFirstTime())
                .destination(config().destination())
                .range(config().range())
                .sound(soundOrThrow())
                .filter(fReceiver -> fakeMessage || socialService.canSeeVanished(fPlayer, fReceiver))
                .integration();

        if (isProxyMode()) {
            eventMetadataBuilder.proxy(dataOutputStream -> {
                dataOutputStream.writeBoolean(hasPlayedBefore);
                dataOutputStream.writeBoolean(fakeMessage);
                dataOutputStream.writeBoolean(vanished);
            });
        }

        messageDispatcher.dispatch(this, JoinMetadata.<Localization.Message.Join>builder()
                .base(eventMetadataBuilder.build())
                .playedBefore(hasPlayedBefore)
                .fakeMessage(fakeMessage)
                .vanished(vanished)
                .build()
        );
    }

}
