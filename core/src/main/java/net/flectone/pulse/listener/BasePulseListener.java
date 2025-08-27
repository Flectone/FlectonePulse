package net.flectone.pulse.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.event.message.PreMessageSendEvent;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerPersistAndDisposeEvent;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.sender.IntegrationSender;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;

@Singleton
public class BasePulseListener implements PulseListener {
    
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ProxySender proxySender;
    private final IntegrationSender integrationSender;
    private final SoundPlayer soundPlayer;

    @Inject
    public BasePulseListener(FPlayerService fPlayerService,
                             PlatformPlayerAdapter platformPlayerAdapter,
                             ProxySender proxySender,
                             IntegrationSender integrationSender,
                             SoundPlayer soundPlayer) {
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.proxySender = proxySender;
        this.integrationSender = integrationSender;
        this.soundPlayer = soundPlayer;
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
    public void onPreMessageSendEvent(PreMessageSendEvent event) {
        MessageType messageType = event.getMessageType();
        String rawFormat = event.getRawFormat();
        EventMetadata<?> eventMetadata = event.getEventMetadata();

        integrationSender.send(messageType, rawFormat, eventMetadata);

        if (proxySender.send(messageType, eventMetadata)) {
            event.setCancelled(true);
        }
    }
}
