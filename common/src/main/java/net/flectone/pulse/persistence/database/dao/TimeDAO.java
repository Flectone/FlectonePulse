package net.flectone.pulse.persistence.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.PlayTime;
import net.flectone.pulse.persistence.database.DatabaseImpl;
import net.flectone.pulse.persistence.database.sql.time.*;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TimeDAO implements BaseDAO<TimeSQL> {

    private final DatabaseImpl database;

    @Override
    public DatabaseImpl database() {
        return database;
    }

    @Override
    public Class<? extends TimeSQL> sqlClass() {
        return switch (database.config().type()) {
            case H2 -> TimeH2.class;
            case MARIADB -> TimeMariaDB.class;
            case MYSQL -> TimeMySQL.class;
            case POSTGRESQL -> TimePostgreSQL.class;
            case SQLITE -> TimeSQLite.class;
        };
    }

    public void saveJoin(@NonNull FPlayer fPlayer) {
        if (database.isClosed()) return;
        if (fPlayer.isUnknown()) return;

        saveSession(new PlayTime(-1, fPlayer.id(), System.currentTimeMillis(), System.currentTimeMillis(), 0, 1));
    }

    public void saveSession(@NonNull PlayTime playTime) {
        if (database.isClosed()) return;
        if (playTime.id() != -1) return;

        useHandle(sql -> sql.upsert(
                playTime.playerId(),
                playTime.first(),
                playTime.last(),
                playTime.total(),
                playTime.sessions()
        ));
    }

    public void saveAfk(@NonNull FPlayer fPlayer, boolean afk, PlayTime playTime) {
        if (database.isClosed()) return;
        if (fPlayer.isUnknown()) return;
        if (playTime == null) return;

        long currentTime = System.currentTimeMillis();

        if (afk) {
            long newTotal = playTime.total() + (currentTime - playTime.last());
            useHandle(sql -> sql.updateLastSeen(-playTime.last(), newTotal, fPlayer.id()));
        } else {
            useHandle(sql -> sql.updateLastSeen(currentTime, playTime.total(), fPlayer.id()));
        }
    }

    public void saveQuit(@NonNull FPlayer fPlayer, PlayTime playTime) {
        if (database.isClosed()) return;
        if (fPlayer.isUnknown()) return;
        if (playTime == null || playTime.last() < 0) return;

        long currentTime = System.currentTimeMillis();
        long newTotal = playTime.total() + (currentTime - playTime.last());
        useHandle(sql -> sql.updateLastSeen(currentTime, newTotal, fPlayer.id()));
    }

    public @NonNull Optional<PlayTime> getByPlayer(@NonNull FPlayer fPlayer) {
        if (database.isClosed()) return Optional.empty();
        if (fPlayer.isUnknown()) return Optional.empty();

        return withHandle(sql -> sql.findByPlayer(fPlayer.id()));
    }

    public int getTotalCount() {
        if (database.isClosed()) return 0;

        return withHandle(TimeSQL::getTotalCount);
    }

    public @NonNull List<PlayTime> getAllPlayTimes(int limit, int offset) {
        if (database.isClosed()) return List.of();

        return withHandle(timeSQL -> timeSQL.getAllPlayTimes(limit, offset));
    }

}