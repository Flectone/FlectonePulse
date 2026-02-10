package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.ModerationSQL;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Data Access Object for moderation data in FlectonePulse.
 * Handles player moderation actions like kicks, bans, mutes, and warnings.
 *
 * @author TheFaser
 * @since 0.9.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModerationDAO implements BaseDAO<ModerationSQL> {

    private final Database database;

    @Override
    public Database database() {
        return database;
    }

    @Override
    public Class<ModerationSQL> sqlClass() {
        return ModerationSQL.class;
    }

    /**
     * Gets all moderation actions for a player.
     *
     * @param player the player
     * @param type the moderation type
     * @return list of moderation actions, empty list if player is unknown
     */
    public List<Moderation> get(@NonNull FPlayer player, Moderation.Type type) {
        if (player.isUnknown()) return Collections.emptyList();
        return withHandle(sql -> sql.findByPlayerAndType(player.getId(), type.ordinal()));
    }

    /**
     * Gets valid moderation actions for a player.
     *
     * @param player the player
     * @param type the moderation type
     * @return list of valid moderation actions, empty list if player is unknown
     */
    public List<Moderation> getValid(@NonNull FPlayer player, Moderation.Type type) {
        if (player.isUnknown()) return Collections.emptyList();
        return withHandle(sql -> sql.findValidByPlayerAndType(
                player.getId(),
                type.ordinal(),
                System.currentTimeMillis()
        ));
    }

    /**
     * Gets all valid moderation actions of a type.
     *
     * @param type the moderation type
     * @return list of valid moderation actions
     */
    public List<Moderation> getValid(Moderation.Type type) {
        return withHandle(sql -> sql.findValidByType(
                type.ordinal(),
                System.currentTimeMillis()
        ));
    }

    /**
     * Gets names of players with valid moderation actions of a type.
     *
     * @param type the moderation type
     * @return list of player names
     */
    public List<String> getValidPlayersNames(Moderation.Type type) {
        return withHandle(sql -> sql.findValidPlayerNamesByType(
                type.ordinal(),
                System.currentTimeMillis()
        ));
    }

    /**
     * Inserts a new moderation action.
     *
     * @param target the target player
     * @param time the expiration timestamp (-1 for permanent)
     * @param reason the moderation reason
     * @param moderatorId the moderator ID
     * @param type the moderation type
     * @return the created moderation action, or null if player is unknown
     */
    public @Nullable Moderation insert(@NonNull FPlayer target, long time, String reason, int moderatorId, Moderation.Type type) {
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

    /**
     * Invalidates a moderation action.
     *
     * @param moderation the moderation action to invalidate
     */
    public void updateValid(@NonNull Moderation moderation) {
        useHandle(sql -> sql.invalidate(moderation.id()));
    }

}