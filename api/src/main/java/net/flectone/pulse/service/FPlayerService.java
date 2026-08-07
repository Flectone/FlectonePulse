package net.flectone.pulse.service;

import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

/**
 * Central service for managing player data across the FlectonePulse plugin.
 * Provides methods for retrieving, caching, and updating player information.
 * Acts as a facade layer between platform-specific player adapters and data repositories,
 * handling cache management and data synchronization.
 * <p>
 * Players can be retrieved using various identifiers such as UUID, name, IP address,
 * database ID, or platform-specific player objects. The service maintains separate
 * caches for online and offline players to optimize performance.
 * </p>
 *
 * @see FPlayer
 * @see net.flectone.pulse.persistence.repository.FPlayerRepository
 *
 * @author TheFaser
 * @since 0.0.1
 */
public interface FPlayerService {

    /**
     * Invalidates all cached player data and reloads from scratch.
     * Clears console player, all platform players, and empties the cache.
     * Typically used during plugin reload or initialization.
     */
    void invalidate();

    /**
     * Invalidates a specific player from all caches.
     *
     * @param uuid the UUID of the player to invalidate
     */
    void invalidate(@NonNull UUID uuid);

    /**
     * Clears all cached player data from both online and offline caches.
     * This operation removes all players from memory but does not affect the database.
     */
    void invalidateCache();

    /**
     * Loads all online players from the database into the cache.
     */
    void loadOnlineCache();

    /**
     * Adds the console player to the cache with configured console name.
     * Creates a new console FPlayer if it doesn't exist, or ignores if already present.
     */
    void addConsole();

    /**
     * Saves or updates a player in the database.
     *
     * @param uuid the player's UUID
     * @param name the player's name
     * @param ip the player's IP address, can be null
     * @param online whether the player is currently online
     * @return the created or updated FPlayer object with assigned database ID
     */
    @NonNull
    FPlayer saveOrUpdate(@NonNull UUID uuid, @NonNull String name, @Nullable String ip, boolean online);

    /**
     * Adds a player to the online cache.
     *
     * @param fPlayer the player to add to cache
     * @return the same player instance that was added
     */
    @NonNull
    FPlayer addCache(@NonNull FPlayer fPlayer);

    /**
     * Updates an existing player in the cache.
     * Preserves online/offline status based on which cache the player is in.
     *
     * @param fPlayer the player data to update in cache
     * @return the same player instance that was updated
     */
    @NonNull
    FPlayer updateCache(FPlayer fPlayer);

    /**
     * Initializes all online platform players by loading their data and dispatching PlayerLoadEvent.
     * Players with cancelled events are invalidated from cache.
     *
     * @param reload whether this is a reload operation or initial startup
     */
    void initialize(SocialService socialService, boolean reload);

    /**
     * Removes a player from offline cache and optionally ensures online status for proxy players.
     *
     * @param uuid the UUID of the player to remove from offline cache
     */
    void invalidateOfflineCache(@NonNull UUID uuid);

    /**
     * Removes a player from online cache.
     *
     * @param uuid the UUID of the player to remove from online cache
     */
    void invalidateOnlineCache(@NonNull UUID uuid);

    /**
     * Clears a player's online status and saves them to offline cache and database.
     * Sets online to false, removes from online cache, updates database, and adds to offline cache.
     *
     * @param fPlayer the player to clear and save as offline
     * @return the updated player with online=false
     */
    @NonNull
    FPlayer clearAndSave(@NonNull FPlayer fPlayer);

    /**
     * Gets a player by database ID. Returns console player if ID is -1.
     *
     * @param id the database ID of the player
     * @return the player or console player if ID is -1
     */
    @NonNull
    FPlayer getFPlayer(int id);

    /**
     * Gets the console player instance.
     *
     * @return the console FPlayer
     */
    @NonNull
    FPlayer getConsole();

    /**
     * Gets a player by name.
     *
     * @param name the player's name
     * @return the player or UNKNOWN if not found
     */
    @NonNull
    FPlayer getFPlayer(@NonNull String name);

    /**
     * Gets a player by IP address.
     *
     * @param inetAddress the player's IP address
     * @return the player or UNKNOWN if not found
     */
    @NonNull
    FPlayer getFPlayer(InetAddress inetAddress);

    /**
     * Gets a player by UUID.
     *
     * @param uuid the player's UUID
     * @return the player or UNKNOWN if not found
     */
    @NonNull
    FPlayer getFPlayer(UUID uuid);

    /**
     * Gets a player from an FEntity by extracting its UUID.
     *
     * @param fEntity the entity to get the player for
     * @return the player associated with the entity's UUID
     */
    @NonNull
    FPlayer getFPlayer(FEntity fEntity);

    /**
     * Gets a player from a platform-specific player object (Bukkit, Fabric, etc.).
     * Handles console detection and creates temporary FPlayer for unknown players.
     *
     * @param platformPlayer the platform-specific player object
     * @return the FPlayer, console player, or a temporary player if not found
     */
    @NonNull
    FPlayer getFPlayer(@NonNull Object platformPlayer);

    /**
     * Gets a random online player from platform players.
     *
     * @return a random FPlayer or UNKNOWN if no players are online
     */
    @NonNull
    FPlayer getRandomFPlayer();

    /**
     * Gets the total count of players with the specified IP address.
     *
     * @param ip the IP address to count players for
     * @return the total number of players with the given IP
     */
    int getTotalFPlayersCountByIp(@NonNull String ip);

    /**
     * Gets a paginated list of players with the specified IP address.
     *
     * @param ip the IP address to filter players by
     * @param limit the maximum number of players to return
     * @param offset the number of players to skip
     * @return list of players matching the IP, ordered by ID descending
     */
    @NonNull
    List<FPlayer> getFPlayersByIp(String ip, int limit, int offset);

    /**
     * Gets all players from the database.
     *
     * @return list of all FPlayers in the database
     */
    @NonNull
    List<FPlayer> findAllFPlayers();

    /**
     * Gets all online players from the cache.
     *
     * @return list of online FPlayers from cache
     */
    @NonNull
    List<FPlayer> getOnlineFPlayers();

    /**
     * Gets all online players that are actually connected to the platform.
     * Filters cached online players by checking their actual platform online status.
     *
     * @return list of platform-verified online FPlayers
     */
    @NonNull
    List<FPlayer> getPlatformFPlayers();

    /**
     * Gets all online players including the console player.
     *
     * @return list of online FPlayers plus console
     */
    @NonNull
    List<FPlayer> getFPlayersWithConsole();

}
