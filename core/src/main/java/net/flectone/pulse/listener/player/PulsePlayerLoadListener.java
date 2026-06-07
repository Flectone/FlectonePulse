package net.flectone.pulse.listener.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.service.PlaytimeService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulsePlayerLoadListener implements PulseListener {

    private final FileFacade fileFacade;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PlatformServerAdapter platformServerAdapter;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final PlaytimeService playtimeService;
    private final SocialService socialService;

    @Pulse(priority = Event.Priority.LOW)
    public PlayerLoadEvent onPlayerLoadEvent(PlayerLoadEvent event) {
        FPlayer fPlayer = event.player();

        UUID currentPlayerUUID = fPlayer.uuid();
        String currentPlayerName = platformPlayerAdapter.getName(currentPlayerUUID);
        String currentPlayerIp = platformPlayerAdapter.getIp(currentPlayerUUID);

        // update moderations
        moderationService.invalidate(currentPlayerUUID);

        // check uuid and name
        boolean mismatchedName = !fPlayer.name().equalsIgnoreCase(currentPlayerName);
        if (fPlayer.isUnknown() || mismatchedName) {
            // invalidate real player uuid in cache
            fPlayerService.invalidate(currentPlayerUUID);
        }

        // insert or update player record in database
        fPlayer = fPlayerService.saveOrUpdate(currentPlayerUUID, currentPlayerName, currentPlayerIp, true);

        // maybe database closed and we need cancel this
        if (fPlayer.isUnknown()) {
            return event.withPlayer(fPlayer).withCancelled(true);
        }

        // save to cache
        fPlayer = fPlayerService.addCache(fPlayer);

        // update settings, colors, ignores cache
        socialService.invalidate(currentPlayerUUID);

        // minecraft locale will update via PacketEvents
        if (platformServerAdapter.getPlatformType() == PlatformType.HYTALE) {
            String currentPlayerLocale = platformPlayerAdapter.getLocale(currentPlayerUUID);
            socialService.updateLocale(fPlayer, currentPlayerLocale);
        }

        // update player server
        String currentServer = fileFacade.config().server();
        if (!currentServer.equals(socialService.getSetting(fPlayer, SettingText.SERVER))) {
            socialService.saveSetting(fPlayer, SettingText.SERVER, currentServer);
        }

        // update play time in database
        if (event.reload()) {
            playtimeService.updateLastSession(fPlayer);
        } else {
            playtimeService.updateJoinSession(fPlayer);
        }

        return event.withPlayer(fPlayer);
    }

}
