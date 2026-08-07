package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.repository.PlaytimeRepository;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.PlayTime;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlaytimeServiceImpl implements PlaytimeService {

    private final FileFacade fileFacade;
    private final PlaytimeRepository playtimeRepository;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Override
    public void initialize() {
        if (!isPlaytimeTracking()) return;

        // more like a migration for older versions below 1.9.0,
        // because this information was not in the database before.
        // and also we cannot add information about all players,
        // because players may not be in the database
        if (getPlayTimesCount() == 0) {
            fPlayerService.findAllFPlayers().forEach(fPlayer -> {
                if (fPlayer.isUnknown() || fPlayer.isConsole()) return;

                PlayTime platformPlayTime = platformPlayerAdapter.getPlayedTime(fPlayer);
                if (platformPlayTime == null) return;

                playtimeRepository.saveJoinSession(platformPlayTime);
            });
        }
    }

    @Override
    public void invalidate(@NonNull UUID uuid) {
        if (isPlaytimeTracking()) {
            playtimeRepository.invalidate(uuid);
        }
    }

    @Override
    public void saveAfkSession(FPlayer fPlayer, boolean afk) {
        if (isPlaytimeTracking()) {
            playtimeRepository.saveAfkSession(fPlayer, afk);
            playtimeRepository.invalidate(fPlayer.uuid());
        }
    }

    @Override
    public void updateJoinSession(@NonNull FPlayer fPlayer) {
        if (isPlaytimeTracking()) {
            playtimeRepository.saveJoinSession(fPlayer);
            playtimeRepository.invalidate(fPlayer.uuid());
        }
    }

    @Override
    public void updateLastSession(@NonNull FPlayer fPlayer) {
        if (isPlaytimeTracking()) {
            playtimeRepository.saveLastSeen(fPlayer);
            playtimeRepository.invalidate(fPlayer.uuid());
        }
    }

    @Override
    public @Nullable PlayTime getPlayTime(FPlayer fPlayer) {
        return isPlaytimeTracking() ? playtimeRepository.getPlayTime(fPlayer) : null;
    }

    @Override
    public int getPlayTimesCount() {
        return isPlaytimeTracking() ? playtimeRepository.getPlayTimesCount() : -1;
    }

    @Override
    @NonNull
    public List<PlayTime> getAllPlayTimes(int limit, int offset) {
        return isPlaytimeTracking() ? playtimeRepository.getAllPlayTimes(limit, offset) : List.of();
    }

    private boolean isPlaytimeTracking() {
        return fileFacade.config().database().usePlaytimeTracking();
    }

}
