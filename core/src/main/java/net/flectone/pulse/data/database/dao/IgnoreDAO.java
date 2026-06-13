package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.ignore.*;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Data Access Object for player ignore data in FlectonePulse.
 * Handles ignore relationships between players.
 *
 * @author TheFaser
 * @since 0.9.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IgnoreDAO implements BaseDAO<IgnoreSQL> {

    private final Database database;

    @Override
    public Database database() {
        return database;
    }

    @Override
    public Class<? extends IgnoreSQL> sqlClass() {
        return switch (database.config().type()) {
            case H2 -> IgnoreH2.class;
            case MARIADB -> IgnoreMariaDB.class;
            case MYSQL -> IgnoreMySQL.class;
            case POSTGRESQL -> IgnorePostgreSQL.class;
            case SQLITE -> IgnoreSQLite.class;
        };
    }

    /**
     * Inserts or updates an ignore relationship between players.
     *
     * @param fSender the player who is ignoring
     * @param fIgnored the player being ignored
     * @return the ignore record, or null if players are unknown
     */
    public @Nullable Ignore insert(@NonNull FPlayer fSender, @NonNull FPlayer fIgnored) {
        if (database.isClosed()) return null;
        if (fSender.isUnknown() || fIgnored.isUnknown()) return null;

        return inTransaction(sql -> {
            long currentTime = System.currentTimeMillis();

            sql.upsert(currentTime, fSender.id(), fIgnored.id());

            return sql.findByInitiatorAndTarget(fSender.id(), fIgnored.id()).orElseThrow();
        });
    }

    /**
     * Invalidates an ignore record.
     *
     * @param ignore the ignore record to invalidate
     */
    public void invalidate(@NonNull Ignore ignore) {
        if (database.isClosed()) return;

        useHandle(sql -> sql.invalidate(ignore.id()));
    }

    public List<Ignore> load(@NonNull FPlayer fPlayer) {
        if (database.isClosed()) return List.of();
        if (fPlayer.isUnknown()) return List.of();

        return withHandle(sql -> sql.findByInitiator(fPlayer.id()));
    }
}