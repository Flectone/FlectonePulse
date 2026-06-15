package net.flectone.pulse.module.message.quit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.message.quit.listener.PulseQuitListener;
import net.flectone.pulse.module.message.quit.listener.QuitProxyMessageListener;
import net.flectone.pulse.module.message.quit.model.QuitMetadata;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.proxy.RedisProxy;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class QuitModule implements ModuleLocalization<Localization.Message.Quit> {

    private final FileFacade fileFacade;
    private final TaskScheduler taskScheduler;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final PlatformServerAdapter platformServerAdapter;
    private final ProxyRegistry proxyRegistry;
    private final FPlayerService fPlayerService;
    private final SocialService socialService;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        if (isProxyMode()) {
            listenerRegistry.register(QuitProxyMessageListener.class);
        }

        listenerRegistry.register(PulseQuitListener.class);
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_QUIT;
    }

    @Override
    public Message.Quit config() {
        return fileFacade.message().quit();
    }

    @Override
    public Permission.Message.Quit permission() {
        return fileFacade.permission().message().quit();
    }

    @Override
    public Localization.Message.Quit localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().quit();
    }

    public boolean isProxyMode() {
        return moduleController.isEnable(this) && config().range().type() == Range.Type.PROXY && proxyRegistry.hasEnabledProxy(proxy -> !(proxy instanceof RedisProxy));
    }

    public void send(FPlayer fPlayer, boolean fakeMessage) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        boolean vanished = socialService.isVanished(fPlayer);
        if (!isProxyMode() || fakeMessage) {
            send(fPlayer, fakeMessage, vanished);
            return;
        }

        UUID playerUUID = fPlayer.uuid();

        // server does not accept requests from proxy, if there are no players
        if (platformServerAdapter.isOnlyPlayerOnline(playerUUID)) {
            // server can check player is offline (and has not reconnected to another server) only from database
            taskScheduler.runAsyncLater(() -> {
                FPlayer cacheFPlayer = fPlayerService.getFPlayer(playerUUID);
                if (!cacheFPlayer.isOnline()) {
                    send(cacheFPlayer, false, vanished);
                }
            }, 100L);
        }
    }

    public void send(FPlayer fPlayer, boolean fakeMessage, boolean vanished) {
        messageDispatcher.dispatch(this, QuitMetadata.<Localization.Message.Quit>builder()
                .base(EventMetadata.<Localization.Message.Quit>builder()
                        .sender(fPlayer)
                        .format(Localization.Message.Quit::format)
                        .destination(config().destination())
                        .range(config().range().is(Range.Type.PROXY) && !fakeMessage ? Range.get(Range.Type.SERVER) : config().range())
                        .sound(soundOrThrow())
                        .filter(fReceiver -> fakeMessage || socialService.canSeeVanished(fPlayer, fReceiver))
                        .integration()
                        .proxy(dataOutputStream -> {
                            dataOutputStream.writeBoolean(fakeMessage);
                            dataOutputStream.writeBoolean(vanished);
                        })
                        .build()
                )
                .fakeMessage(fakeMessage)
                .vanished(vanished)
                .build()
        );
    }

}
