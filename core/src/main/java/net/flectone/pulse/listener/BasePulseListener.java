package net.flectone.pulse.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.lifecycle.DisableEvent;
import net.flectone.pulse.model.event.lifecycle.EnableEvent;
import net.flectone.pulse.model.event.message.MessagePrepareEvent;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerPersistAndDisposeEvent;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.sender.IntegrationSender;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BasePulseListener implements PulseListener {
    
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ProxySender proxySender;
    private final IntegrationSender integrationSender;
    private final SoundPlayer soundPlayer;

    @Pulse(priority = Event.Priority.LOWEST, ignoreCancelled = true)
    public PlayerJoinEvent onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.player().withIp(platformPlayerAdapter.getIp(event.player()));

        fPlayerService.saveFPlayerData(fPlayer);

        return event.withPlayer(fPlayer);
    }

    @Pulse
    public PlayerPersistAndDisposeEvent onPlayerPersistAndDispose(PlayerPersistAndDisposeEvent event) {
        FPlayer fPlayer = fPlayerService.clearAndSave(event.player());

        return event.withPlayer(fPlayer);
    }

    @Pulse
    public void onMessageSendEvent(MessageSendEvent event) {
        EventMetadata<?> eventMetadata = event.eventMetadata();
        if (eventMetadata.sound() != null) {
            soundPlayer.play(eventMetadata.sound(), eventMetadata.sender(), event.receiver());
        }
    }

    @Pulse
    public Event onMessagePrepareEvent(MessagePrepareEvent event) {
        MessageType messageType = event.messageType();
        String rawFormat = event.rawFormat();
        EventMetadata<?> eventMetadata = event.eventMetadata();

        integrationSender.asyncSend(messageType, rawFormat, eventMetadata);

        if (proxySender.send(messageType, eventMetadata)) {
            return event.withCancelled(true);
        }

        return event;
    }

    @Pulse
    public void onEnableEvent(EnableEvent event) {
        integrationSender.send(MessageType.SERVER_ENABLE, "", EventMetadata.builder()
                .sender(FPlayer.UNKNOWN)
                .format("")
                .integration()
                .build()
        );
    }

    @Pulse
    public void onDisableEvent(DisableEvent event) {
        integrationSender.send(MessageType.SERVER_DISABLE, "", EventMetadata.builder()
                .sender(FPlayer.UNKNOWN)
                .format("")
                .integration()
                .build()
        );
    }
}
