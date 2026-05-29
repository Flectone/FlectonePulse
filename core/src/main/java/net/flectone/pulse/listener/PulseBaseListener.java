package net.flectone.pulse.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.MessagePrepareEvent;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerPersistAndDisposeEvent;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;

import java.util.Objects;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseBaseListener implements PulseListener {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ProxySender proxySender;
    private final SoundPlayer soundPlayer;
    private final TaskScheduler taskScheduler;

    @Pulse(priority = Event.Priority.LOWEST, ignoreCancelled = true)
    public PlayerJoinEvent onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.player();

        String platformIp = platformPlayerAdapter.getIp(fPlayer);
        boolean anotherIp = !Objects.equals(fPlayer.ip(), platformIp);
        if (anotherIp) {
            fPlayer = fPlayer.withIp(platformIp);
        }

        String server = fileFacade.config().server();
        boolean anotherServer = !Objects.equals(fPlayer.getSetting(SettingText.SERVER), server);
        if (anotherServer) {
            fPlayer = fPlayer.withSetting(SettingText.SERVER, server);
        }

        FPlayer finalFPlayer = fPlayer;
        taskScheduler.runAsync(() -> {
            if (anotherIp) {
                fPlayerService.saveFPlayerData(finalFPlayer);
            }

            if (anotherServer) {
                fPlayerService.saveOrUpdateSetting(finalFPlayer, SettingText.SERVER);
            }
        });

        return event.withPlayer(finalFPlayer);
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

    @Pulse(priority = Event.Priority.HIGH)
    public Event onMessagePrepareEvent(MessagePrepareEvent event) {
        if (event.isForProxy() && proxySender.send(event.moduleName(), event.eventMetadata())) {
            return event.withCancelled(true);
        }

        return event;
    }

}
