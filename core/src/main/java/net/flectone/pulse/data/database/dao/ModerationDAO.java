package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.ModerationSQL;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModerationDAO extends BaseDAO<ModerationSQL> {

    private final Database database;

    @Override
    public Database database() {
        return database;
    }

    @Override
    public Class<ModerationSQL> sqlClass() {
        return ModerationSQL.class;
    }

    public List<Moderation> get(FPlayer player, Moderation.Type type) {
        if (player.isUnknown()) return List.of();
        return withHandle(sql -> sql.findByPlayerAndType(player.getId(), type.ordinal()));
    }

    public List<Moderation> getValid(FPlayer player, Moderation.Type type) {
        if (player.isUnknown()) return List.of();
        return withHandle(sql -> sql.findValidByPlayerAndType(
                player.getId(),
                type.ordinal(),
                System.currentTimeMillis()
        ));
    }

    public List<Moderation> getValid(Moderation.Type type) {
        return withHandle(sql -> sql.findValidByType(
                type.ordinal(),
                System.currentTimeMillis()
        ));
    }

    public List<String> getValidPlayersNames(Moderation.Type type) {
        return withHandle(sql -> sql.findValidPlayerNamesByType(
                type.ordinal(),
                System.currentTimeMillis()
        ));
    }

    @Nullable
    public Moderation insert(FPlayer target, long time, String reason,
                             int moderatorId, Moderation.Type type) {
        if (target.isUnknown()) return null;

        return inTransaction(sql -> {
            long date = System.currentTimeMillis();
            int id = sql.insert(
                    target.getId(),
                    date,
                    time,
                    reason,
                    moderatorId,
                    type.ordinal()
            );

            return new Moderation(
                    id,
                    target.getId(),
                    date,
                    time,
                    reason,
                    moderatorId,
                    type,
                    true
            );
        });
    }

    public void updateValid(Moderation moderation) {
        useHandle(sql -> sql.invalidate(moderation.getId()));
    }
}