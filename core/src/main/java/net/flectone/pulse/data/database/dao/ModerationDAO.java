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
     * @param server the server ID
     * @return list of moderation actions, empty list if player is unknown
     */
    public List<Moderation> get(@NonNull FPlayer player, Moderation.Type type, @Nullable String server) {
        if (player.isUnknown()) return Collections.emptyList();
        return withHandle(sql -> sql.findByPlayerAndType(player.id(), type.name(), server));
    }

    /**
     * Gets valid moderation actions for a player.
     *
     * @param player the player
     * @param type the moderation type
     * @param server the server ID
     * @return list of valid moderation actions, empty list if player is unknown
     */
    public List<Moderation> getValid(@NonNull FPlayer player, Moderation.Type type, @Nullable String server) {
        if (player.isUnknown()) return Collections.emptyList();
        return withHandle(sql -> sql.findValidByPlayerAndType(
                player.id(),
                type.name(),
                System.currentTimeMillis(),
                server
        ));
    }

    /**
     * Gets all valid moderation actions of a type.
     *
     * @param type the moderation type
     * @param server the server ID
     * @return list of valid moderation actions
     */
    public List<Moderation> getValid(Moderation.Type type, @Nullable String server) {
        return withHandle(sql -> sql.findValidByType(
                type.name(),
                System.currentTimeMillis(),
                server
        ));
    }

    /**
     * Gets names of players with valid moderation actions of a type.
     *
     * @param type the moderation type
     * @param server the server ID
     * @return list of player names
     */
    public List<String> getValidPlayersNames(Moderation.Type type, @Nullable String server) {
        return withHandle(sql -> sql.findValidPlayerNamesByType(
                type.name(),
                System.currentTimeMillis(),
                server
        ));
    }

    /**
     * Inserts a new moderation action.
     *
     * @param target the target player
     * @param date the moderation date
     * @param time the expiration timestamp (-1 for permanent)
     * @param reason the moderation reason
     * @param moderatorId the moderator ID
     * @param type the moderation type
     * @param server the server ID
     * @return the created moderation action, or null if player is unknown
     */
    public @Nullable Moderation insert(@NonNull FPlayer target, long date, long time, String reason, int moderatorId, Moderation.Type type, String server) {
        if (target.isUnknown()) return null;

        return inTransaction(sql -> {
            int id = sql.insert(
                    target.id(),
                    date,
                    time,
                    reason,
                    moderatorId,
                    type.name(),
                    server
            );

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