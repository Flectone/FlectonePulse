package net.flectone.pulse.repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.database.dao.ColorsDAO;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.database.dao.SettingDAO;
import net.flectone.pulse.model.FPlayer;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Singleton
public class FPlayerRepository {

    private final Map<UUID, FPlayer> onlinePlayers = new ConcurrentHashMap<>();
    private final Cache<UUID, FPlayer> offlinePlayersCache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .maximumSize(1000)
            .build();

    private final FPlayerDAO fPlayerDAO;
    private final SettingDAO settingDAO;
    private final ColorsDAO colorsDAO;

    @Inject
    public FPlayerRepository(FPlayerDAO fPlayerDAO,
                             SettingDAO settingDAO,
                             ColorsDAO colorsDAO) {
        this.fPlayerDAO = fPlayerDAO;
        this.settingDAO = settingDAO;
        this.colorsDAO = colorsDAO;
    }

    public void invalid(UUID uuid) {
        onlinePlayers.remove(uuid);
        offlinePlayersCache.invalidate(uuid);
    }

    public FPlayer get(int id) {
        Optional<FPlayer> onlinePlayer = onlinePlayers.values()
                .stream()
                .filter(p -> p.getId() == id)
                .findFirst();
        if (onlinePlayer.isPresent()) return onlinePlayer.get();

        Optional<FPlayer> cachedPlayer = offlinePlayersCache.asMap().values()
                .stream()
                .filter(p -> p.getId() == id)
                .findFirst();
        if (cachedPlayer.isPresent()) return cachedPlayer.get();

        FPlayer dbPlayer = fPlayerDAO.getFPlayer(id);
        saveToCache(dbPlayer);

        return dbPlayer;
    }

    public FPlayer get(InetAddress inetAddress) {
        String ip = inetAddress.getHostAddress();

        Optional<FPlayer> onlinePlayer = onlinePlayers.values()
                .stream()
                .filter(p -> ip.equals(p.getIp()))
                .findFirst();
        if (onlinePlayer.isPresent()) return onlinePlayer.get();

        Optional<FPlayer> cachedPlayer = offlinePlayersCache.asMap().values()
                .stream()
                .filter(p -> ip.equals(p.getIp()))
                .findFirst();
        if (cachedPlayer.isPresent()) return cachedPlayer.get();

        FPlayer dbPlayer = fPlayerDAO.getFPlayer(inetAddress);
        saveToCache(dbPlayer);

        return dbPlayer;
    }

    public FPlayer get(UUID uuid) {
        FPlayer onlinePlayer = onlinePlayers.get(uuid);
        if (onlinePlayer != null) return onlinePlayer;

        FPlayer cachedPlayer = offlinePlayersCache.getIfPresent(uuid);
        if (cachedPlayer != null) return cachedPlayer;

        FPlayer dbPlayer = fPlayerDAO.getFPlayer(uuid);
        saveToCache(dbPlayer);

        return dbPlayer;
    }

    public FPlayer get(String playerName) {
        Optional<FPlayer> onlinePlayer = onlinePlayers.values()
                .stream()
                .filter(p -> p.getName().equalsIgnoreCase(playerName))
                .findFirst();
        if (onlinePlayer.isPresent()) return onlinePlayer.get();

        Optional<FPlayer> cachedPlayer = offlinePlayersCache.asMap().values()
                .stream()
                .filter(p -> p.getName().equalsIgnoreCase(playerName))
                .findFirst();
        if (cachedPlayer.isPresent()) return cachedPlayer.get();

        FPlayer dbPlayer = fPlayerDAO.getFPlayer(playerName);
        saveToCache(dbPlayer);

        return dbPlayer;
    }

    private void saveToCache(FPlayer fPlayer) {
        if (fPlayer.isOnline()) {
            onlinePlayers.put(fPlayer.getUuid(), fPlayer);
        } else {
            offlinePlayersCache.put(fPlayer.getUuid(), fPlayer);
        }
    }

    public boolean save(UUID uuid, String name) {
        return fPlayerDAO.insert(uuid, name);
    }

    public void update(FPlayer fPlayer) {
        fPlayerDAO.update(fPlayer);
    }

    public void saveOrIgnore(FPlayer fPlayer) {
        fPlayerDAO.insertOrIgnore(fPlayer);
    }

    public void removeOffline(UUID uuid) {
        offlinePlayersCache.invalidate(uuid);
    }

    public void removeOnline(UUID uuid) {
        FPlayer fPlayer = onlinePlayers.get(uuid);
        if (fPlayer != null) {
            fPlayer.setOnline(false);
            offlinePlayersCache.put(uuid, fPlayer);
        }

        onlinePlayers.remove(uuid);
    }

    public void add(FPlayer fPlayer) {
        onlinePlayers.put(fPlayer.getUuid(), fPlayer);
        offlinePlayersCache.invalidate(fPlayer.getUuid());
    }

    public List<FPlayer> getAllPlayersDatabase() {
        return fPlayerDAO.getFPlayers();
    }

    public List<FPlayer> getOnlinePlayersDatabase() {
        return fPlayerDAO.getOnlineFPlayers();
    }

    public List<FPlayer> getOnlinePlayers() {
        return onlinePlayers.values().stream().filter(FPlayer::isOnline).toList();
    }

    public List<FPlayer> getOnlineFPlayersWithConsole() {
        return onlinePlayers.values().stream().filter(fPlayer -> fPlayer.isOnline() || fPlayer.isUnknown()).toList();
    }

    public void clearCache() {
        onlinePlayers.clear();
        offlinePlayersCache.invalidateAll();
    }

    public void loadColors(FPlayer fPlayer) {
        colorsDAO.load(fPlayer);
    }

    public void saveColors(FPlayer fPlayer) {
        colorsDAO.save(fPlayer);
    }

    public void saveSettings(FPlayer fPlayer) {
        settingDAO.save(fPlayer);
    }

    public void deleteSetting(FPlayer fPlayer, FPlayer.Setting setting) {
        settingDAO.delete(fPlayer, setting);
    }

    public void loadSettings(FPlayer fPlayer) {
        settingDAO.load(fPlayer);
    }

    public void saveOrUpdateSetting(FPlayer fPlayer, FPlayer.Setting setting) {
        settingDAO.insertOrUpdate(fPlayer, setting);
    }
}
