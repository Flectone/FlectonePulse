package net.flectone.pulse.data.repository;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.dao.ColorsDAO;
import net.flectone.pulse.data.database.dao.FPlayerDAO;
import net.flectone.pulse.data.database.dao.SettingDAO;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.constant.SettingText;
import org.jspecify.annotations.NonNull;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    private final @Named("offlinePlayers") Cache<UUID, FPlayer> offlinePlayersCache;
    private final FPlayerDAO fPlayerDAO;
    private final SettingDAO settingDAO;
    private final ColorsDAO colorsDAO;

    /**
     * Invalidates a player from all caches.
     *
     * @param uuid the player UUID to invalidate
     */
    public void invalid(@NonNull UUID uuid) {
        onlinePlayers.remove(uuid);
        offlinePlayersCache.invalidate(uuid);
    }

    /**
     * Gets a player by database ID with caching.
     *
     * @param id the player database ID
     * @return the player
     */
    public FPlayer get(int id) {
        Optional<FPlayer> onlinePlayer = onlinePlayers.values()
                .stream()
                .filter(fPlayer -> fPlayer.id() == id)
                .findFirst();
        if (onlinePlayer.isPresent()) return onlinePlayer.get();

        Optional<FPlayer> cachedPlayer = offlinePlayersCache.asMap().values()
                .stream()
                .filter(fPlayer -> fPlayer.id() == id)
                .findFirst();
        if (cachedPlayer.isPresent()) return cachedPlayer.get();

        FPlayer dbPlayer = fPlayerDAO.getFPlayer(id);
        saveToCache(dbPlayer);

        return dbPlayer;
    }

    /**
     * Gets a player by IP address with caching.
     *
     * @param inetAddress the IP address
     * @return the player
     */
    public FPlayer get(@NonNull InetAddress inetAddress) {
        String ip = inetAddress.getHostAddress();

        Optional<FPlayer> onlinePlayer = onlinePlayers.values()
                .stream()
                .filter(fPlayer -> ip.equals(fPlayer.ip()))
                .findFirst();
        if (onlinePlayer.isPresent()) return onlinePlayer.get();

        Optional<FPlayer> cachedPlayer = offlinePlayersCache.asMap().values()
                .stream()
                .filter(fPlayer -> ip.equals(fPlayer.ip()))
                .findFirst();
        if (cachedPlayer.isPresent()) return cachedPlayer.get();

        FPlayer dbPlayer = fPlayerDAO.getFPlayer(inetAddress);
        saveToCache(dbPlayer);

        return dbPlayer;
    }

    /**
     * Gets a player by UUID with caching.
     *
     * @param uuid the player UUID
     * @return the player
     */
    public FPlayer get(@NonNull UUID uuid) {
        FPlayer onlinePlayer = onlinePlayers.get(uuid);
        if (onlinePlayer != null) return onlinePlayer;

        FPlayer cachedPlayer = offlinePlayersCache.getIfPresent(uuid);
        if (cachedPlayer != null) return cachedPlayer;

        FPlayer dbPlayer = fPlayerDAO.getFPlayer(uuid);
        saveToCache(dbPlayer);

        return dbPlayer;
    }

    /**
     * Gets a player by name with caching.
     *
     * @param playerName the player name
     * @return the player
     */
    public FPlayer get(@NonNull String playerName) {
        Optional<FPlayer> onlinePlayer = onlinePlayers.values()
                .stream()
                .filter(fPlayer -> fPlayer.name().equalsIgnoreCase(playerName))
                .findFirst();
        if (onlinePlayer.isPresent()) return onlinePlayer.get();

        Optional<FPlayer> cachedPlayer = offlinePlayersCache.asMap().values()
                .stream()
                .filter(fPlayer -> fPlayer.name().equalsIgnoreCase(playerName))
                .findFirst();
        if (cachedPlayer.isPresent()) return cachedPlayer.get();

        FPlayer dbPlayer = fPlayerDAO.getFPlayer(playerName);
        saveToCache(dbPlayer);

        return dbPlayer;
    }

    private void saveToCache(FPlayer fPlayer) {
        if (fPlayer.isOnline()) {
            onlinePlayers.put(fPlayer.uuid(), fPlayer);
        } else {
            offlinePlayersCache.put(fPlayer.uuid(), fPlayer);
        }
    }

    /**
     * Saves a new player to the database.
     *
     * @param uuid the player UUID
     * @param name the player name
     * @return true if a new player was inserted, false if existing player was updated
     */
    public boolean save(@NonNull UUID uuid, @NonNull String name) {
        return fPlayerDAO.insert(uuid, name);
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
     * Saves a player or ignores if already exists.
     *
     * @param fPlayer the player to save
     */
    public void saveOrIgnore(@NonNull FPlayer fPlayer) {
        fPlayerDAO.insertOrIgnore(fPlayer);
    }

    /**
     * Removes a player from the offline cache.
     *
     * @param uuid the player UUID
     */
    public void removeOffline(@NonNull UUID uuid) {
        offlinePlayersCache.invalidate(uuid);
    }

    /**
     * Moves a player from online to offline cache.
     *
     * @param uuid the player UUID
     */
    public void removeOnline(@NonNull UUID uuid) {
        FPlayer fPlayer = onlinePlayers.get(uuid);
        if (fPlayer != null) {
            offlinePlayersCache.put(uuid, fPlayer.withOnline(false));
        }

        onlinePlayers.remove(uuid);
    }

    public void removeOnline(@NonNull FPlayer fPlayer) {
        onlinePlayers.remove(fPlayer.uuid());

        offlinePlayersCache.put(fPlayer.uuid(), fPlayer.isOnline() ? fPlayer.withOnline(false) : fPlayer);
    }

    /**
     * Adds a player to the online cache.
     *
     * @param fPlayer the player to add
     */
    public void add(@NonNull FPlayer fPlayer) {
        onlinePlayers.put(fPlayer.uuid(), fPlayer);
        offlinePlayersCache.invalidate(fPlayer.uuid());
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
        return fPlayerDAO.getOnlineFPlayers();
    }

    /**
     * Gets all online players from the cache.
     *
     * @return list of online players
     */
    public List<FPlayer> getOnlinePlayers() {
        return onlinePlayers.values().stream().filter(FPlayer::isOnline).toList();
    }

    /**
     * Gets all online players plus the console.
     *
     * @return list of online players and console
     */
    public List<FPlayer> getOnlineFPlayersWithConsole() {
        return onlinePlayers.values().stream().filter(fPlayer -> fPlayer.isOnline() || fPlayer.isConsole()).toList();
    }

    /**
     * Clears all caches.
     */
    public void clearCache() {
        onlinePlayers.clear();
        offlinePlayersCache.invalidateAll();
    }

    /**
     * Loads color settings for a player.
     *
     * @param fPlayer the player to load colors for
     * @return new FPlayer with colors
     */
    public FPlayer loadColors(@NonNull FPlayer fPlayer) {
        return colorsDAO.load(fPlayer);
    }

    /**
     * Saves color settings for a player.
     *
     * @param fPlayer the player to save colors for
     */
    public void saveColors(@NonNull FPlayer fPlayer) {
        colorsDAO.save(fPlayer);
    }

    /**
     * Saves all settings for a player.
     *
     * @param fPlayer the player to save settings for
     */
    public void saveSettings(@NonNull FPlayer fPlayer) {
        settingDAO.save(fPlayer);
    }

    /**
     * Loads all settings for a player.
     *
     * @param fPlayer the player to load settings for
     */
    public FPlayer loadSettings(@NonNull FPlayer fPlayer) {
        return settingDAO.load(fPlayer);
    }

    /**
     * Saves or updates a specific setting for a player.
     *
     * @param fPlayer the player
     * @param setting the setting name
     */
    public void saveOrUpdateSetting(@NonNull FPlayer fPlayer, @NonNull String setting) {
        settingDAO.insertOrUpdate(fPlayer, setting);
    }

    /**
     * Saves or updates a specific setting for a player.
     *
     * @param fPlayer the player
     * @param setting the setting text
     */
    public void saveOrUpdateSetting(@NonNull FPlayer fPlayer, @NonNull SettingText setting) {
        settingDAO.insertOrUpdate(fPlayer, setting);
    }

    /**
     * Updates the cache with the latest player data
     *
     * @param fPlayer the player data to update in cache
     */
    public void updateCache(FPlayer fPlayer) {
        if (onlinePlayers.containsKey(fPlayer.uuid())) {
            onlinePlayers.put(fPlayer.uuid(), fPlayer);
            return;
        }

        FPlayer offlineFPlayer = offlinePlayersCache.getIfPresent(fPlayer.uuid());
        if (offlineFPlayer != null) {
            offlinePlayersCache.put(fPlayer.uuid(), fPlayer.isOnline() ? fPlayer.withOnline(false) : fPlayer);
        }
    }
}