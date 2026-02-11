package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.IgnoreSQL;
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
    public Class<IgnoreSQL> sqlClass() {
        return IgnoreSQL.class;
    }

    /**
     * Inserts or updates an ignore relationship between players.
     *
     * @param fSender the player who is ignoring
     * @param fIgnored the player being ignored
     * @return the ignore record, or null if players are unknown
     */
    public @Nullable Ignore insert(@NonNull FPlayer fSender, @NonNull FPlayer fIgnored) {
        if (fSender.isUnknown() || fIgnored.isUnknown()) return null;

        return inTransaction(sql -> {
            long currentTime = System.currentTimeMillis();
            int updated = sql.update(currentTime, fSender.id(), fIgnored.id());

            if (updated == 0) {
                int insertedId = sql.insert(currentTime, fSender.id(), fIgnored.id());
                return new Ignore(insertedId, currentTime, fIgnored.id());
            } else {
                return sql.findByInitiatorAndTarget(fSender.id(), fIgnored.id()).orElseThrow();
            }
        });
    }

    /**
     * Invalidates an ignore record.
     *
     * @param ignore the ignore record to invalidate
     */
    public void invalidate(@NonNull Ignore ignore) {
        useHandle(sql -> sql.invalidate(ignore.id()));
    }

    /**
     * Loads ignore relationships for a player.
     *
     * @param fPlayer the player to load ignores for
     * @return new FPlayer with ignores
     */
    public FPlayer load(@NonNull FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return fPlayer;

        List<Ignore> ignores = withHandle(sql ->
                sql.findByInitiator(fPlayer.id())
        );

        return fPlayer.withIgnores(ignores);
    }
}