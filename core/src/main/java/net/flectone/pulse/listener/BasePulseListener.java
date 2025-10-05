package net.flectone.pulse.listener;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.MessagePrepareEvent;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.event.module.ModuleEnableEvent;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerPersistAndDisposeEvent;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.command.online.OnlineModule;
import net.flectone.pulse.module.command.toponline.ToponlineModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.tab.TabModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.sender.IntegrationSender;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class BasePulseListener implements PulseListener {
    
    private final FPlayerService fPlayerService;
    private final PlatformServerAdapter platformServerAdapter;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ProxySender proxySender;
    private final IntegrationSender integrationSender;
    private final SoundPlayer soundPlayer;
    private final PacketProvider packetProvider;
    private final FLogger fLogger;

    @Inject
    public BasePulseListener(FPlayerService fPlayerService,
                             PlatformServerAdapter platformServerAdapter,
                             PlatformPlayerAdapter platformPlayerAdapter,
                             ProxySender proxySender,
                             IntegrationSender integrationSender,
                             SoundPlayer soundPlayer,
                             PacketProvider packetProvider,
                             FLogger fLogger) {
        this.fPlayerService = fPlayerService;
        this.platformServerAdapter = platformServerAdapter;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.proxySender = proxySender;
        this.integrationSender = integrationSender;
        this.soundPlayer = soundPlayer;
        this.packetProvider = packetProvider;
        this.fLogger = fLogger;
    }

    @Pulse(priority = Event.Priority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.getPlayer();

        // set correct ip
        fPlayer.setIp(platformPlayerAdapter.getIp(fPlayer));

        fPlayerService.saveFPlayerData(fPlayer);
    }

    @Pulse
    public void onPlayerPersistAndDispose(PlayerPersistAndDisposeEvent event) {
        FPlayer fPlayer = event.getPlayer();
        fPlayerService.clearAndSave(fPlayer);
    }

    @Pulse
    public void onMessageSendEvent(MessageSendEvent event) {
        EventMetadata<?> eventMetadata = event.getEventMetadata();
        if (eventMetadata.getSound() != null) {
            Sound sound = eventMetadata.getSound();

            soundPlayer.play(sound, eventMetadata.getSender(), event.getReceiver());
        }
    }

    @Pulse
    public void onMessagePrepareEvent(MessagePrepareEvent event) {
        MessageType messageType = event.getMessageType();
        String rawFormat = event.getRawFormat();
        EventMetadata<?> eventMetadata = event.getEventMetadata();

        integrationSender.send(messageType, rawFormat, eventMetadata);

        if (proxySender.send(messageType, eventMetadata)) {
            event.setCancelled(true);
        }
    }

    @Pulse
    public void onModuleEnableEvent(ModuleEnableEvent event) {
        AbstractModule eventModule = event.getModule();
        if (eventModule instanceof BubbleModule
                && packetProvider.getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_12_2)) {
            fLogger.warning("Bubble module is not supported on this version of Minecraft");
            event.setCancelled(true);
            return;
        }

        if (eventModule instanceof TabModule
                && packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_9)
                && packetProvider.getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_9_4)) {
            fLogger.warning("TAB module is not supported on this version of Minecraft");
            event.setCancelled(true);
            return;
        }

        if (eventModule instanceof OnlineModule
                && platformServerAdapter.getPlatformType() == PlatformType.FABRIC) {
            fLogger.warning("Online module is not supported on Fabric");
            event.setCancelled(true);
            return;
        }

        if (eventModule instanceof ToponlineModule
                && platformServerAdapter.getPlatformType() == PlatformType.FABRIC) {
            fLogger.warning("Toponline module is not supported on Fabric");
            event.setCancelled(true);
        }
    }
}
