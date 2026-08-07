package net.flectone.pulse.data.repository;

import net.flectone.pulse.model.entity.FPlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

/**
 * Repository for managing player data in FlectonePulse.
 * Provides caching and retrieval of player information from various sources.
 *
 * @author TheFaser
 * @since 0.8.1
 */
public interface FPlayerRepository {

    /**
     * Invalidates a player from all caches.
     *
     * @param uuid the player UUID to invalidate
     */
    void invalid(@NonNull UUID uuid);

    /**
     * Gets a player by database ID with caching.
     *
     * @param id the player database ID
     * @return the player
     */
    FPlayer get(int id);

    /**
     * Gets a player by IP address with caching.
     *
     * @param inetAddress the IP address
     * @return the player
     */
    FPlayer get(@NonNull InetAddress inetAddress);

    /**
     * Gets a player by UUID with caching.
     *
     * @param uuid the player UUID
     * @return the player
     */
    FPlayer get(@NonNull UUID uuid);

    /**
     * Gets a player by name with caching.
     *
     * @param playerName the player name
     * @return the player
     */
    FPlayer get(@NonNull String playerName);

    /**
     * Reads a player straight from the database, bypassing the cache.
     *
     * @param uuid the account id
     * @return the player, or the unknown player if there is no row
     */
    FPlayer getFromDatabase(UUID uuid);

    /**
     * Reads a player from the cache only.
     *
     * @param uuid the account id, may be null
     * @return the player, or the unknown player if they are not cached
     */
    FPlayer getFromCache(@Nullable UUID uuid);

    /**
     * Removes a player from the offline cache.
     *
     * @param uuid the player UUID
     */
    void removeOffline(@NonNull UUID uuid);

    /**
     * Moves a player from online to offline cache.
     *
     * @param uuid the player UUID
     */
    void removeOnline(@NonNull UUID uuid);

    /**
     * Moves a player from online to offline cache.
     *
     * @param fPlayer the player
     */
    void removeOnline(@NonNull FPlayer fPlayer);

    /**
     * Adds a player to the online cache.
     *
     * @param fPlayer the player to add
     */
    void add(@NonNull FPlayer fPlayer);

    /**
     * Updates the cache with the latest player data
     *
     * @param fPlayer the player data to update in cache
     */
    void updateCache(FPlayer fPlayer);

    /**
     * Clears all caches
     */
    void clearCache();

    /**
     * Saves or updates a player in the database.
     *
     * @param uuid the player's UUID
     * @param name the player's name
     * @param ip the player's IP address, can be null
     * @param online whether the player is currently online
     * @return the created or updated FPlayer object with assigned database ID
     */
    FPlayer saveOrUpdate(@NonNull UUID uuid, @NonNull String name, @Nullable String ip, boolean online);

    /**
     * Updates a player in the database.
     *
     * @param fPlayer the player to update
     */
    void update(@NonNull FPlayer fPlayer);

    /**
     * Sets all players associated with the specified server to offline status.
     *
     * @param server the server identifier to match against player server settings
     */
    void setOfflineByServer(@NonNull String server);

    /**
     * Saves a player or ignores if already exists.
     *
     * @param fPlayer the player to save
     */
    void saveOrIgnore(@NonNull FPlayer fPlayer);

    /**
     * Gets the total count of players with the specified IP address.
     *
     * @param ip the IP address to count players for
     * @return the total number of players with the given IP
     */
    int getTotalPlayersCountByIp(@NonNull String ip);

    /**
     * Gets a paginated list of players with the specified IP address.
     *
     * @param ip the IP address to filter players by
     * @param limit the maximum number of players to return
     * @param offset the number of players to skip
     * @return list of players matching the IP, ordered by ID descending
     */
    List<FPlayer> getPlayersByIp(@NonNull String ip, int limit, int offset);

    /**
     * Gets all players from the database.
     *
     * @return list of all players
     */
    List<FPlayer> getAllPlayersDatabase();

    /**
     * Gets all online players from the database.
     *
     * @return list of online players
     */
    List<FPlayer> getOnlinePlayersDatabase();

    /**
     * Gets all online players from the cache.
     *
     * @return list of online players
     */
    List<FPlayer> getOnlinePlayers();

    /**
     * Gets all online players plus the console.
     *
     * @return list of online players and console
     */
    List<FPlayer> getOnlineFPlayersWithConsole();

}
