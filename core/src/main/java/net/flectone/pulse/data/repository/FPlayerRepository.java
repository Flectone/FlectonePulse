package net.flectone.pulse.data.repository;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.dao.FPlayerDAO;
import net.flectone.pulse.model.entity.FPlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository for managing player data in FlectonePulse.
 * Provides caching and retrieval of player information from various sources.
 *
 * @author TheFaser
 * @since 0.8.1
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FPlayerRepository {

    private final Map<UUID, FPlayer> onlinePlayers = new ConcurrentHashMap<>();

    private final Map<String, UUID> nameToUuidIndex = new ConcurrentHashMap<>();
    private final Map<Integer, UUID> idToUuidIndex = new ConcurrentHashMap<>();
    private final Map<String, UUID> ipToUuidIndex = new ConcurrentHashMap<>();

    private final @Named("offlinePlayers") Cache<UUID, FPlayer> offlinePlayersCache;
    private final FPlayerDAO fPlayerDAO;

    /**
     * Invalidates a player from all caches.
     *
     * @param uuid the player UUID to invalidate
     */
    public void invalid(@NonNull UUID uuid) {
        FPlayer fPlayer = onlinePlayers.remove(uuid);
        if (fPlayer != null) {
            removeFromIndexes(fPlayer);
        }

        fPlayer = offlinePlayersCache.getIfPresent(uuid);
        if (fPlayer != null) {
            offlinePlayersCache.invalidate(uuid);
            removeFromIndexes(fPlayer);
        }
    }

    /**
     * Gets a player by database ID with caching.
     *
     * @param id the player database ID
     * @return the player
     */
    public FPlayer get(int id) {
        UUID uuid = idToUuidIndex.get(id);

        FPlayer cache = getFromCache(uuid);
        if (cache != null) return cache;

        FPlayer fPlayer = fPlayerDAO.getFPlayer(id);
        saveToCache(fPlayer);

        return fPlayer;
    }

    /**
     * Gets a player by IP address with caching.
     *
     * @param inetAddress the IP address
     * @return the player
     */
    public FPlayer get(@NonNull InetAddress inetAddress) {
        String ip = inetAddress.getHostAddress();

        UUID uuid = ipToUuidIndex.get(ip);

        FPlayer cache = getFromCache(uuid);
        if (cache != null) return cache;

        FPlayer fPlayer = fPlayerDAO.getFPlayer(inetAddress);
        saveToCache(fPlayer);

        return fPlayer;
    }

    /**
     * Gets a player by UUID with caching.
     *
     * @param uuid the player UUID
     * @return the player
     */
    public FPlayer get(@NonNull UUID uuid) {
        FPlayer cacheOnline = onlinePlayers.get(uuid);
        if (cacheOnline != null) return cacheOnline;

        FPlayer cacheOffline = offlinePlayersCache.getIfPresent(uuid);
        if (cacheOffline != null) return cacheOffline;

        FPlayer fPlayer = getFromDatabase(uuid);
        saveToCache(fPlayer);

        return fPlayer;
    }

    /**
     * Gets a player by name with caching.
     *
     * @param playerName the player name
     * @return the player
     */
    public FPlayer get(@NonNull String playerName) {
        UUID uuid = nameToUuidIndex.get(playerName.toLowerCase());

        FPlayer cache = getFromCache(uuid);
        if (cache != null) return cache;

        FPlayer fPlayer = fPlayerDAO.getFPlayer(playerName);
        saveToCache(fPlayer);

        return fPlayer;
    }

    @NonNull
    public FPlayer getFromDatabase(UUID uuid) {
        return fPlayerDAO.getFPlayer(uuid);
    }

    @Nullable
    public FPlayer getFromCache(@Nullable UUID uuid) {
        if (uuid == null) return null;

        FPlayer fPlayer = onlinePlayers.get(uuid);
        if (fPlayer != null) return fPlayer;

        return offlinePlayersCache.getIfPresent(uuid);
    }

    /**
     * Removes a player from the offline cache.
     *
     * @param uuid the player UUID
     */
    public void removeOffline(@NonNull UUID uuid) {
        FPlayer offlineFPlayer = offlinePlayersCache.getIfPresent(uuid);
        if (offlineFPlayer == null) return;

        offlinePlayersCache.invalidate(uuid);

        FPlayer fPlayer = get(uuid);
        saveToCacheOnline(fPlayer);
    }

    /**
     * Moves a player from online to offline cache.
     *
     * @param uuid the player UUID
     */
    public void removeOnline(@NonNull UUID uuid) {
        FPlayer fPlayer = onlinePlayers.remove(uuid);
        if (fPlayer != null) {
            removeOnline(fPlayer);
        }
    }

    /**
     * Moves a player from online to offline cache.
     *
     * @param fPlayer the player
     */
    public void removeOnline(@NonNull FPlayer fPlayer) {
        onlinePlayers.remove(fPlayer.uuid());
        saveToCacheOffline(fPlayer);
    }

    /**
     * Adds a player to the online cache.
     *
     * @param fPlayer the player to add
     */
    public void add(@NonNull FPlayer fPlayer) {
        onlinePlayers.put(fPlayer.uuid(), fPlayer);
        addToIndexes(fPlayer);
        offlinePlayersCache.invalidate(fPlayer.uuid());
    }

    /**
     * Updates the cache with the latest player data
     *
     * @param fPlayer the player data to update in cache
     */
    public void updateCache(FPlayer fPlayer) {
        FPlayer cacheFPlayer = onlinePlayers.get(fPlayer.uuid());
        if (cacheFPlayer != null) {
            offlinePlayersCache.invalidate(fPlayer.uuid());
            saveToCacheOnline(fPlayer);
            return;
        }

        cacheFPlayer = offlinePlayersCache.getIfPresent(fPlayer.uuid());
        if (cacheFPlayer != null) {
            saveToCacheOffline(fPlayer);
        }
    }

    /**
     * Clears all caches
     */
    public void clearCache() {
        onlinePlayers.clear();
        offlinePlayersCache.invalidateAll();
        nameToUuidIndex.clear();
        idToUuidIndex.clear();
        ipToUuidIndex.clear();
    }

    /**
     * Saves or updates a player in the database.
     *
     * @param uuid the player's UUID
     * @param name the player's name
     * @param ip the player's IP address, can be null
     * @param online whether the player is currently online
     * @return the created or updated FPlayer object with assigned database ID
     */
    public FPlayer saveOrUpdate(@NonNull UUID uuid, @NonNull String name, @Nullable String ip, boolean online) {
        return fPlayerDAO.insertOrUpdate(uuid, name, ip, online);
    }

    /**
     * Updates a player in the database.
     *
     * @param fPlayer the player to update
     */
    public void update(@NonNull FPlayer fPlayer) {
        fPlayerDAO.update(fPlayer);
    }

    /**
     * Sets all players associated with the specified server to offline status.
     *
     * @param server the server identifier to match against player server settings
     */
    public void setOfflineByServer(@NonNull String server) {
        fPlayerDAO.setOfflineByServer(server);
    }

    /**
     * Saves a player or ignores if already exists.
     *
     * @param fPlayer the player to save
     */
    public void saveOrIgnore(@NonNull FPlayer fPlayer) {
        fPlayerDAO.insertOrIgnore(fPlayer);
    }

    /**
     * Gets all players from the database.
     *
     * @return list of all players
     */
    public List<FPlayer> getAllPlayersDatabase() {
        return fPlayerDAO.getFPlayers();
    }

    /**
     * Gets all online players from the database.
     *
     * @return list of online players
     */
    public List<FPlayer> getOnlinePlayersDatabase() {
        return fPlayerDAO.getOnlineFPlayers().stream()
                .filter(fPlayer -> !fPlayer.isConsole())
                .toList();
    }

    /**
     * Gets all online players from the cache.
     *
     * @return list of online players
     */
    public List<FPlayer> getOnlinePlayers() {
        return onlinePlayers.values().stream()
                .filter(FPlayer::isOnline)
                .toList();
    }

    /**
     * Gets all online players plus the console.
     *
     * @return list of online players and console
     */
    public List<FPlayer> getOnlineFPlayersWithConsole() {
        return onlinePlayers.values().stream()
                .filter(fPlayer -> fPlayer.isOnline() || fPlayer.isConsole())
                .toList();
    }

    private void saveToCache(FPlayer fPlayer) {
        if (fPlayer.isOnline() || fPlayer.isConsole()) {
            saveToCacheOnline(fPlayer);
        } else {
            if (fPlayer.id().equals(FPlayer.UNKNOWN.id())
                    && fPlayer.uuid().equals(FPlayer.UNKNOWN.uuid())
                    && fPlayer.name().equals(FPlayer.UNKNOWN.name())
                    && fPlayer.ip() == null) return;

            // save only changed player
            saveToCacheOffline(fPlayer);
        }
    }

    private void saveToCacheOnline(FPlayer fPlayer) {
        removeFromIndexes(fPlayer);

        onlinePlayers.put(fPlayer.uuid(), fPlayer);

        addToIndexes(fPlayer);
    }

    private void saveToCacheOffline(FPlayer fPlayer) {
        removeFromIndexes(fPlayer);

        FPlayer offlineFPlayer = fPlayer.isOnline() ? fPlayer.withOnline(false) : fPlayer;

        offlinePlayersCache.put(fPlayer.uuid(), offlineFPlayer);

        addToIndexes(offlineFPlayer);
    }

    private void addToIndexes(FPlayer fPlayer) {
        UUID uuid = fPlayer.uuid();
        nameToUuidIndex.put(fPlayer.name().toLowerCase(), uuid);
        idToUuidIndex.put(fPlayer.id(), uuid);
        if (fPlayer.ip() != null) {
            ipToUuidIndex.put(fPlayer.ip(), uuid);
        }
    }

    private void removeFromIndexes(FPlayer fPlayer) {
        nameToUuidIndex.remove(fPlayer.name().toLowerCase());
        idToUuidIndex.remove(fPlayer.id());
        if (fPlayer.ip() != null) {
            ipToUuidIndex.remove(fPlayer.ip());
        }
    }
}