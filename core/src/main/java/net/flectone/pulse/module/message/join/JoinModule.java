package net.flectone.pulse.module.message.join;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.util.PlayTime;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.join.model.JoinMetadata;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.IntegrationSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

// Proxy mode implementation may seem strange and inefficient, but it's the only way (at least for PLUGIN_MESSAGE mode)
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class JoinModule implements ModuleLocalization<Localization.Message.Join> {

    private final Set<UUID> proxyConnectMessagePlayers = new CopyOnWriteArraySet<>();

    private final FileFacade fileFacade;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final IntegrationModule integrationModule;
    private final TaskScheduler taskScheduler;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final FPlayerService fPlayerService;
    private final IntegrationSender integrationSender;
    private final ProxyRegistry proxyRegistry;

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
    public Localization.Message.Join localization(FEntity sender) {
        return fileFacade.localization(sender).message().join();
    }

    public boolean isProxyMode() {
        return moduleController.isEnable(this) && config().range().type() == Range.Type.PROXY && proxyRegistry.hasEnabledProxy();
    }

    public void proxySend(UUID uuid) {
        if (isProxyMode()) {
            // indicator for messages for integration,
            proxyConnectMessagePlayers.add(uuid);
            taskScheduler.runAsyncLater(() -> proxyConnectMessagePlayers.remove(uuid), 40L);

            privateSend(fPlayerService.getFPlayer(uuid), Range.get(Range.Type.SERVER), false, false);
        }
    }

    public void sendLater(FPlayer fPlayer) {
        if (isProxyMode()) {
            taskScheduler.runAsyncLater(() -> {
                if (proxyConnectMessagePlayers.remove(fPlayer.uuid())) {
                    sendToIntegration(fPlayer);
                }
            });

            return;
        }

        taskScheduler.runRegionLater(fPlayer, () -> privateSend(fPlayer, config().range(), true, false), 5L);
    }

    public void send(FPlayer fPlayer, boolean ignoreVanish) {
        if (isProxyMode() && !ignoreVanish) {
            taskScheduler.runAsyncLater(() -> {
                if (proxyConnectMessagePlayers.remove(fPlayer.uuid())) {
                    sendToIntegration(fPlayer);
                }
            });

            return;
        }

        taskScheduler.runRegion(fPlayer, () -> privateSend(fPlayer, config().range(), !ignoreVanish, ignoreVanish));
    }

    private void privateSend(FPlayer fPlayer, Range range, boolean toIntegration, boolean ignoreVanish) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        messageDispatcher.dispatch(this, buildEventMetadata(fPlayer, range, toIntegration, ignoreVanish));
    }

    private void sendToIntegration(FPlayer fPlayer) {
        EventMetadata<Localization.Message.Join> eventMetadata = buildEventMetadata(fPlayer, config().range(),true, false);
        integrationSender.send(name(), eventMetadata.resolveFormat(FPlayer.UNKNOWN, localization()), eventMetadata);
    }

    private EventMetadata<Localization.Message.Join> buildEventMetadata(FPlayer fPlayer, Range range, boolean toIntegration, boolean ignoreVanish) {
        PlayTime playTime = fPlayerService.getPlayTime(fPlayer);
        boolean hasPlayedBefore = platformPlayerAdapter.hasPlayedBefore(fPlayer) || (playTime != null && playTime.sessions() > 1);

        EventMetadata.Builder<Localization.Message.Join> eventMetadata = EventMetadata.<Localization.Message.Join>builder()
                .sender(fPlayer)
                .format(localization -> hasPlayedBefore || !config().first() ? localization.format() : localization.formatFirstTime())
                .destination(config().destination())
                .range(range)
                .sound(soundOrThrow())
                .filter(fReceiver -> ignoreVanish || integrationModule.canSeeVanished(fPlayer, fReceiver))
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeBoolean(hasPlayedBefore);
                    dataOutputStream.writeBoolean(ignoreVanish);
                });

        if (toIntegration) {
            eventMetadata.integration();
        }

        return JoinMetadata.<Localization.Message.Join>builder()
                .base(eventMetadata.build())
                .ignoreVanish(ignoreVanish)
                .build();
    }
}
