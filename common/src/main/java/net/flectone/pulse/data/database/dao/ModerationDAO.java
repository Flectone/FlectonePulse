package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.DatabaseImpl;
import net.flectone.pulse.data.database.sql.moderation.ModerationSQL;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModerationDAO implements BaseDAO<ModerationSQL> {

    private final DatabaseImpl database;

    @Override
    public DatabaseImpl database() {
        return database;
    }

    @Override
    public Class<? extends ModerationSQL> sqlClass() {
        return ModerationSQL.class;
    }

    public List<Moderation> getValid(@NonNull FPlayer player, Moderation.Type type, @Nullable String server, int limit, int offset) {
        if (database.isClosed()) return List.of();
        if (player.isUnknown()) return List.of();

        return withHandle(sql -> sql.findValidByPlayerAndType(
                player.id(),
                type.name(),
                System.currentTimeMillis(),
                server,
                limit,
                offset
        ));
    }

    public List<Moderation> getValid(Moderation.Type type, @Nullable String server, int limit, int offset) {
        if (database.isClosed()) return List.of();

        return withHandle(sql -> sql.findValidByType(
                type.name(),
                System.currentTimeMillis(),
                server,
                limit,
                offset
        ));
    }

    public Optional<Moderation> getValidById(@Nullable String server, int id) {
        if (database.isClosed()) return Optional.empty();

        return withHandle(sql -> sql.findValidById(
                System.currentTimeMillis(),
                server,
                id
        ));
    }

    public List<String> getValidPlayersNames(Moderation.Type type, @Nullable String server) {
        if (database.isClosed()) return List.of();

        return withHandle(sql -> sql.findValidPlayerNamesByType(
                type.name(),
                System.currentTimeMillis(),
                server
        ));
    }

    public int getTotalValidCount(FPlayer fPlayer, Moderation.Type type, @Nullable String server) {
        if (database.isClosed()) return 0;

        return withHandle(sql -> sql.getTotalValidCountByPlayerAndType(
                fPlayer.id(),
                type.name(),
                System.currentTimeMillis(),
                server
        ));
    }

    public int getTotalValidCount(Moderation.Type type, @Nullable String server) {
        if (database.isClosed()) return 0;

        return withHandle(sql -> sql.getTotalValidCountByType(
                type.name(),
                System.currentTimeMillis(),
                server
        ));
    }

    public @Nullable Moderation insert(@NonNull FPlayer target, long date, long time, String reason, int moderatorId, Moderation.Type type, String server) {
        if (database.isClosed()) return null;
        if (target.isUnknown()) return null;

        int id = withHandle(sql -> sql.insert(target.id(), date, time, reason, moderatorId, type.name(), server));

        return new Moderation(
                id,
                target.id(),
                date,
                time,
                reason,
                moderatorId,
                type,
                true,
                server
        );
    }

    public void updateValid(int id, @Nullable String server) {
        if (database.isClosed()) return;

        useHandle(sql -> sql.invalidate(id, server));
    }

    public void updateValid(int playerId, Moderation.@NonNull Type type, @Nullable String server) {
        if (database.isClosed()) return;

        useHandle(sql -> sql.invalidate(playerId, type.name(), server));
    }

}