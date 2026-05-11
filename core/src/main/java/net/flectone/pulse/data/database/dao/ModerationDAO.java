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
import java.util.Optional;

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
     * Retrieves valid (non-expired) moderation actions for a specific player and type with pagination.
     * Returns an empty list if the player is unknown.
     *
     * @param player the player to retrieve moderations for
     * @param type the moderation type to filter by
     * @param server the server ID (can be null for global search)
     * @param limit maximum number of results to return
     * @param offset number of results to skip for pagination
     * @return list of valid moderation actions matching the criteria
     */
    public List<Moderation> getValid(@NonNull FPlayer player, Moderation.Type type, @Nullable String server, int limit, int offset) {
        if (player.isUnknown()) return Collections.emptyList();
        return withHandle(sql -> sql.findValidByPlayerAndType(
                player.id(),
                type.name(),
                System.currentTimeMillis(),
                server,
                limit,
                offset
        ));
    }

    /**
     * Retrieves valid (non-expired) moderation actions by type across all players with pagination.
     *
     * @param type the moderation type to filter by
     * @param server the server ID (can be null for global search)
     * @param limit maximum number of results to return
     * @param offset number of results to skip for pagination
     * @return list of valid moderation actions matching the criteria
     */
    public List<Moderation> getValid(Moderation.Type type, @Nullable String server, int limit, int offset) {
        return withHandle(sql -> sql.findValidByType(
                type.name(),
                System.currentTimeMillis(),
                server,
                limit,
                offset
        ));
    }

    /**
     * Retrieves a single valid moderation entry by its unique identifier.
     * Checks that the moderation is valid and not expired.
     *
     * @param server the server ID (can be null for global search)
     * @param id the unique moderation entry identifier
     * @return an Optional containing the moderation if found and valid, or empty otherwise
     */
    public Optional<Moderation> getValidById(@Nullable String server, int id) {
        return withHandle(sql -> sql.findValidById(
                System.currentTimeMillis(),
                server,
                id
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
     * Counts the total number of valid moderations for a specific player and type.
     * Only includes non-expired moderations.
     *
     * @param fPlayer the player to count moderations for
     * @param type the moderation type to filter by
     * @param server the server ID (can be null for global count)
     * @return the count of valid moderations matching the criteria
     */
    public int getTotalValidCount(FPlayer fPlayer, Moderation.Type type, @Nullable String server) {
        return withHandle(sql -> sql.getTotalValidCountByPlayerAndType(
                fPlayer.id(),
                type.name(),
                System.currentTimeMillis(),
                server
        ));
    }

    /**
     * Counts the total number of valid moderations by type across all players.
     * Only includes non-expired moderations.
     *
     * @param type the moderation type to filter by
     * @param server the server ID (can be null for global count)
     * @return the count of valid moderations matching the criteria
     */
    public int getTotalValidCount(Moderation.Type type, @Nullable String server) {
        return withHandle(sql -> sql.getTotalValidCountByType(
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
     * Invalidates a specific moderation entry by setting its valid flag to false.
     * This effectively removes it from active moderation lists without deleting the record.
     *
     * @param id the unique moderation entry identifier to invalidate
     * @param server the server ID (can be null for global invalidation)
     */
    public void updateValid(int id, @Nullable String server) {
        useHandle(sql -> sql.invalidate(id, server));
    }

    /**
     * Invalidates all moderation entries of a specific type by setting their valid flag to false.
     * Can be filtered by server to target server-specific moderations only.
     *
     * @param type the moderation type to invalidate
     * @param server the server ID (can be null for global invalidation)
     */
    public void updateValid(Moderation.@NonNull Type type, @Nullable String server) {
        useHandle(sql -> sql.invalidate(type.name(), server));
    }

}