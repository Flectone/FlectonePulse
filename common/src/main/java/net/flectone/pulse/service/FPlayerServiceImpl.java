package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.dispatcher.EventDispatcher;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.persistence.repository.FPlayerRepository;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.util.random.RandomGenerator;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FPlayerServiceImpl implements FPlayerService {

    private final FileFacade fileFacade;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final FPlayerRepository fPlayerRepository;
    private final RandomGenerator randomUtil;
    private final EventDispatcher eventDispatcher;
    private final TaskScheduler taskScheduler;
    private final ProxyRegistry proxyRegistry;
    private final RandomGenerator randomGenerator;

    @Override
    public void invalidate() {
        // invalidate and load console FPlayer to reload name
        fPlayerRepository.invalid(getConsole().uuid());

        // invalidate all platform players
        platformPlayerAdapter.getOnlinePlayers().forEach(fPlayerRepository::invalid);

        // clear cache
        invalidateCache();
    }

    @Override
    public void invalidate(@NonNull UUID uuid) {
        fPlayerRepository.invalid(uuid);
    }

    @Override
    public void invalidateCache() {
        fPlayerRepository.clearCache();
    }

    @Override
    public void loadOnlineCache() {
        fPlayerRepository.getOnlinePlayersDatabase().forEach(this::addCache);
    }

    @Override
    public void addConsole() {
        FPlayer console = FPlayer.builder()
                .id(FPlayer.CONSOLE_ID)
                .name(fileFacade.config().logger().console())
                .type(FPlayer.CONSOLE_TYPE)
                .build();

        fPlayerRepository.saveOrIgnore(console);

        addCache(console);
    }

    @Override
    @NonNull
    public FPlayer saveOrUpdate(@NonNull UUID uuid, @NonNull String name, @Nullable String ip, boolean online) {
        return fPlayerRepository.saveOrUpdate(uuid, name, ip, online);
    }

    @Override
    @NonNull
    public FPlayer addCache(@NonNull FPlayer fPlayer) {
        fPlayerRepository.add(fPlayer);
        return fPlayer;
    }

    @Override
    @NonNull
    public FPlayer updateCache(FPlayer fPlayer) {
        fPlayerRepository.updateCache(fPlayer);
        return fPlayer;
    }

    @Override
    public void initialize(SocialService socialService, boolean reload) {
        // force offline all players tied to this server
        // in case of an unexpected shutdown that left them marked as online
        fPlayerRepository.setOfflineByServer(fileFacade.config().server());

        // load all platform players
        platformPlayerAdapter.getOnlinePlayers().forEach(uuid -> {
            FPlayer fPlayer = getFPlayer(uuid);
            PlayerLoadEvent playerLoadEvent = eventDispatcher.dispatch(new PlayerLoadEvent(fPlayer, reload));
            if (playerLoadEvent.cancelled()) {
                invalidate(uuid);
            }
        });

        // I hate Minecraft player sync logic. When a player is already on the server
        // and a new player with the same nickname joins, JOIN fires BEFORE quit
        // this leaves the player offline despite still being on the server
        // the same issue occurs with the proxy server
        taskScheduler.runAsyncTimer(() -> {
            // online players of the current server, which are stored in cache
            List<FPlayer> onlineCachedPlayers = fPlayerRepository.getOnlinePlayers();
            Set<UUID> onlineCachedUuids = onlineCachedPlayers.stream()
                    .map(FPlayer::uuid)
                    .collect(Collectors.toSet());

            // online players from the database before we start checking
            List<FPlayer> onlineDatabasePlayers = fPlayerRepository.getOnlinePlayersDatabase();
            Map<UUID, FPlayer> onlineDatabasePlayersByUuid = onlineDatabasePlayers.stream()
                    .collect(Collectors.toMap(FPlayer::uuid, Function.identity()));

            // first, we need to make sure that online players in cache are actually online
            onlineCachedPlayers.forEach(fPlayer -> {
                // check their status in the database
                if (!onlineDatabasePlayersByUuid.containsKey(fPlayer.uuid())) {
                    // and if it's not online, we need to remove them from the cache
                    fPlayerRepository.removeOnline(fPlayer);
                    socialService.invalidate(fPlayer.uuid());
                }
            });

            // next, we need to check that online players in the database are in the local online cache
            onlineDatabasePlayers.forEach(fPlayer -> {
                if (!onlineCachedUuids.contains(fPlayer.uuid())) {
                    // if they are not, we add them
                    addCache(fPlayer);
                    socialService.invalidate(fPlayer.uuid());
                }
            });

            // the most important thing!
            // during these operations, another server may have already changed player's status in cache and database
            // we need to get all online players on the current server and check that their status in the database
            List<UUID> platformPlayers = platformPlayerAdapter.getOnlinePlayers();
            platformPlayers.forEach(uuid -> {
                // if it is already online in the database, nothing to do here
                if (onlineDatabasePlayersByUuid.containsKey(uuid)) return;

                FPlayer fPlayer = fPlayerRepository.getFromDatabase(uuid);

                // if player is not online even in the database, we need to update their status to online
                if (!fPlayer.isOnline()) {
                    fPlayer = fPlayer.withOnline(true);

                    fPlayerRepository.update(fPlayer);
                }

                // finally, we need to add player to online cache
                addCache(fPlayer);
                socialService.invalidate(fPlayer.uuid());
            });
        }, 20L, 20L); // I think 1 second is enough to avoid visual sync problems
    }

    @Override
    public void invalidateOfflineCache(@NonNull UUID uuid) {
        fPlayerRepository.removeOffline(uuid);
    }

    @Override
    public void invalidateOnlineCache(@NonNull UUID uuid) {
        fPlayerRepository.removeOnline(uuid);
    }

    @Override
    @NonNull
    public FPlayer clearAndSave(@NonNull FPlayer fPlayer) {
        // update online
        fPlayer = fPlayer.withOnline(false);

        // remove from online cache
        fPlayerRepository.removeOnline(fPlayer);

        // update status in database
        fPlayerRepository.update(fPlayer);

        // save to database
        fPlayer = updateCache(fPlayer);

        return fPlayer;
    }

    @Override
    @NonNull
    public FPlayer getFPlayer(int id) {
        return fPlayerRepository.get(id);
    }

    @Override
    @NonNull
    public FPlayer getConsole() {
        FPlayer fPlayer = getFPlayer(FEntity.UNKNOWN_UUID);
        if (!fPlayer.isConsole()) {
            return getFPlayer(FPlayer.CONSOLE_ID);
        }

        return fPlayer;
    }

    @Override
    @NonNull
    public FPlayer getFPlayer(@NonNull String name) {
        return fPlayerRepository.get(name);
    }

    @Override
    @NonNull
    public FPlayer getFPlayer(InetAddress inetAddress) {
        return fPlayerRepository.get(inetAddress);
    }

    @Override
    @NonNull
    public FPlayer getFPlayer(UUID uuid) {
        return fPlayerRepository.get(uuid);
    }

    @Override
    @NonNull
    public FPlayer getFPlayer(FEntity fEntity) {
        return getFPlayer(fEntity.uuid());
    }

    @Override
    @NonNull
    public FPlayer getFPlayer(@NonNull Object platformPlayer) {
        String name = platformPlayerAdapter.getName(platformPlayer);
        if (name.isEmpty()) return FPlayer.UNKNOWN;

        UUID uuid = platformPlayerAdapter.getUUID(platformPlayer);
        if (uuid == null) {
            if (platformPlayerAdapter.isConsole(platformPlayer)) {
                return getConsole();
            }

            return FPlayer.builder()
                    .id(randomGenerator.nextInt(Integer.MIN_VALUE, -1))
                    .name(name)
                    .uuid(UUID.randomUUID())
                    .build();
        }

        FPlayer fPlayer = getFPlayer(uuid);
        if (!name.equals(fPlayer.name())) {
            return FPlayer.builder()
                    .id(randomGenerator.nextInt(Integer.MIN_VALUE, -1))
                    .name(name)
                    .uuid(uuid)
                    .type(platformPlayerAdapter.getEntityTranslationKey(platformPlayer))
                    .build();
        }

        return fPlayer;
    }

    @Override
    @NonNull
    public FPlayer getRandomFPlayer() {
        List<FPlayer> fPlayers = getPlatformFPlayers();
        if (fPlayers.isEmpty()) return FPlayer.UNKNOWN;

        int randomIndex = randomUtil.nextInt(0, fPlayers.size());
        return fPlayers.get(randomIndex);
    }

    @Override
    public int getTotalFPlayersCountByIp(@NonNull String ip) {
        return fPlayerRepository.getTotalPlayersCountByIp(ip);
    }

    @Override
    @NonNull
    public List<FPlayer> getFPlayersByIp(String ip, int limit, int offset) {
        return fPlayerRepository.getPlayersByIp(ip, limit, offset);
    }

    @Override
    @NonNull
    public List<FPlayer> findAllFPlayers() {
        return fPlayerRepository.getAllPlayersDatabase();
    }

    @Override
    @NonNull
    public List<FPlayer> getOnlineFPlayers() {
        return fPlayerRepository.getOnlinePlayers();
    }

    @Override
    @NonNull
    public List<FPlayer> getPlatformFPlayers() {
        return fPlayerRepository.getOnlinePlayers().stream()
                .filter(platformPlayerAdapter::isOnline)
                .toList();
    }

    @Override
    @NonNull
    public List<FPlayer> getFPlayersWithConsole() {
        return fPlayerRepository.getOnlineFPlayersWithConsole();
    }

}
