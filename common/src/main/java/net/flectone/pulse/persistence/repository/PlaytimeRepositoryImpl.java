package net.flectone.pulse.persistence.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.PlayTime;
import net.flectone.pulse.persistence.database.dao.TimeDAO;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlaytimeRepositoryImpl implements PlaytimeRepository {

    private static final PlayTime MISSING = new PlayTime(-1, -1, 0, 0, 0, 0);

    private final @Named("playtime") Cache<UUID, PlayTime> playTimeCache;

    private final TimeDAO timeDAO;

    @Override
    public void saveJoinSession(FPlayer fPlayer) {
        timeDAO.saveJoin(fPlayer);
    }

    @Override
    public void saveJoinSession(PlayTime playTime) {
        timeDAO.saveSession(playTime);
    }

    @Override
    public void saveAfkSession(FPlayer fPlayer, boolean afk) {
        timeDAO.saveAfk(fPlayer, afk, getPlayTime(fPlayer));
    }

    @Override
    public void saveLastSeen(FPlayer fPlayer) {
        timeDAO.saveQuit(fPlayer, getPlayTime(fPlayer));
    }

    @Override
    public @Nullable PlayTime getPlayTime(FPlayer fPlayer) {
        PlayTime playTime = playTimeCache.get(fPlayer.uuid(), _ -> timeDAO.getByPlayer(fPlayer).orElse(MISSING));

        return playTime == MISSING ? null : playTime;
    }

    @Override
    public int getPlayTimesCount() {
        return timeDAO.getTotalCount();
    }

    @Override
    public List<PlayTime> getAllPlayTimes(int limit, int offset) {
        return timeDAO.getAllPlayTimes(limit, offset);
    }

    @Override
    public void invalidate(UUID uuid) {
        playTimeCache.invalidate(uuid);
    }

}
