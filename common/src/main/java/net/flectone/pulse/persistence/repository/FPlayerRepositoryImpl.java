package net.flectone.pulse.persistence.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.flectone.pulse.constant.CacheName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.persistence.database.dao.FPlayerDAO;
import net.flectone.pulse.platform.registry.CacheRegistryImpl;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.InetAddress;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class FPlayerRepositoryImpl implements FPlayerRepository {

    private final Map<UUID, FPlayer> onlinePlayers = new ConcurrentHashMap<>();

    private final Map<String, UUID> nameToUuidIndex = new ConcurrentHashMap<>();
    private final Map<Integer, UUID> idToUuidIndex = new ConcurrentHashMap<>();
    private final Map<String, UUID> ipToUuidIndex = new ConcurrentHashMap<>();

    private final @Named("offlinePlayers") Cache<UUID, FPlayer> offlinePlayersCache;
    private final FPlayerDAO fPlayerDAO;

    @Inject
    public FPlayerRepositoryImpl(@Named("offlinePlayers") Cache<UUID, FPlayer> offlinePlayersCache,
                                 FPlayerDAO fPlayerDAO,
                                 CacheRegistryImpl cacheRegistry) {
        this.offlinePlayersCache = offlinePlayersCache;
        this.fPlayerDAO = fPlayerDAO;

        cacheRegistry.<UUID, FPlayer>addRemovalListener(CacheName.OFFLINE_PLAYERS, (_, evicted, _) -> {
            if (evicted != null) {
                removeFromIndexes(evicted);
            }
        });
    }

    @Override
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

    @Override
    public FPlayer get(int id) {
        UUID uuid = idToUuidIndex.get(id);

        FPlayer cache = getFromCache(uuid);
        if (cache != null) return cache;

        FPlayer fPlayer = fPlayerDAO.getFPlayer(id);
        saveToCache(fPlayer);

        return fPlayer;
    }

    @Override
    public FPlayer get(@NonNull InetAddress inetAddress) {
        String ip = inetAddress.getHostAddress();

        UUID uuid = ipToUuidIndex.get(ip);

        FPlayer cache = getFromCache(uuid);
        if (cache != null) return cache;

        FPlayer fPlayer = fPlayerDAO.getFPlayer(inetAddress);
        saveToCache(fPlayer);

        return fPlayer;
    }

    @Override
    public FPlayer get(@NonNull UUID uuid) {
        FPlayer cacheOnline = onlinePlayers.get(uuid);
        if (cacheOnline != null) return cacheOnline;

        FPlayer cacheOffline = offlinePlayersCache.getIfPresent(uuid);
        if (cacheOffline != null) return cacheOffline;

        FPlayer fPlayer = getFromDatabase(uuid);
        saveToCache(fPlayer);

        return fPlayer;
    }

    @Override
    public FPlayer get(@NonNull String playerName) {
        UUID uuid = nameToUuidIndex.get(playerName.toLowerCase(Locale.ROOT));

        FPlayer cache = getFromCache(uuid);
        if (cache != null) return cache;

        FPlayer fPlayer = fPlayerDAO.getFPlayer(playerName);
        saveToCache(fPlayer);

        return fPlayer;
    }

    @NonNull
    @Override
    public FPlayer getFromDatabase(UUID uuid) {
        return fPlayerDAO.getFPlayer(uuid);
    }

    @Nullable
    @Override
    public FPlayer getFromCache(@Nullable UUID uuid) {
        if (uuid == null) return null;

        FPlayer fPlayer = onlinePlayers.get(uuid);
        if (fPlayer != null) return fPlayer;

        return offlinePlayersCache.getIfPresent(uuid);
    }

    @Override
    public void removeOffline(@NonNull UUID uuid) {
        FPlayer offlineFPlayer = offlinePlayersCache.getIfPresent(uuid);
        if (offlineFPlayer == null) return;

        offlinePlayersCache.invalidate(uuid);

        FPlayer fPlayer = get(uuid);
        saveToCacheOnline(fPlayer);
    }

    @Override
    public void removeOnline(@NonNull UUID uuid) {
        FPlayer fPlayer = onlinePlayers.remove(uuid);
        if (fPlayer != null) {
            removeOnline(fPlayer);
        }
    }

    @Override
    public void removeOnline(@NonNull FPlayer fPlayer) {
        onlinePlayers.remove(fPlayer.uuid());
        saveToCacheOffline(fPlayer);
    }

    @Override
    public void add(@NonNull FPlayer fPlayer) {
        onlinePlayers.put(fPlayer.uuid(), fPlayer);
        addToIndexes(fPlayer);
        offlinePlayersCache.invalidate(fPlayer.uuid());
    }

    @Override
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

    @Override
    public void clearCache() {
        onlinePlayers.clear();
        offlinePlayersCache.invalidateAll();
        nameToUuidIndex.clear();
        idToUuidIndex.clear();
        ipToUuidIndex.clear();
    }

    @Override
    public FPlayer saveOrUpdate(@NonNull UUID uuid, @NonNull String name, @Nullable String ip, boolean online) {
        return fPlayerDAO.insertOrUpdate(uuid, name, ip, online);
    }

    @Override
    public void update(@NonNull FPlayer fPlayer) {
        fPlayerDAO.update(fPlayer);
    }

    @Override
    public void setOfflineByServer(@NonNull String server) {
        fPlayerDAO.setOfflineByServer(server);
    }

    @Override
    public void saveOrIgnore(@NonNull FPlayer fPlayer) {
        fPlayerDAO.insertOrIgnore(fPlayer);
    }

    @Override
    public List<FPlayer> getPlayersByIp(@NonNull String ip) {
        return fPlayerDAO.getFPlayersByIp(ip);
    }

    @Override
    public List<FPlayer> getAllPlayersDatabase() {
        return fPlayerDAO.getFPlayers();
    }

    @Override
    public List<FPlayer> getOnlinePlayersDatabase() {
        return fPlayerDAO.getOnlineFPlayers().stream()
                .filter(fPlayer -> !fPlayer.isConsole())
                .toList();
    }

    @Override
    public List<FPlayer> getOnlinePlayers() {
        return onlinePlayers.values().stream()
                .filter(FPlayer::isOnline)
                .toList();
    }

    @Override
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
        nameToUuidIndex.put(fPlayer.name().toLowerCase(Locale.ROOT), uuid);
        idToUuidIndex.put(fPlayer.id(), uuid);
        if (fPlayer.ip() != null) {
            ipToUuidIndex.put(fPlayer.ip(), uuid);
        }
    }

    private void removeFromIndexes(FPlayer fPlayer) {
        UUID uuid = fPlayer.uuid();
        nameToUuidIndex.remove(fPlayer.name().toLowerCase(Locale.ROOT), uuid);
        idToUuidIndex.remove(fPlayer.id(), uuid);
        if (fPlayer.ip() != null) {
            ipToUuidIndex.remove(fPlayer.ip(), uuid);
        }
    }
}