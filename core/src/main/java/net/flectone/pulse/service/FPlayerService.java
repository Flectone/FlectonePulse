package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.repository.FPlayerRepository;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.generator.RandomGenerator;
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
 * @see FPlayerRepository
 *
 * @author TheFaser
 * @since 0.0.1
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FPlayerService {

    private final FileFacade fileFacade;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final FPlayerRepository fPlayerRepository;
    private final RandomGenerator randomUtil;
    private final EventDispatcher eventDispatcher;
    private final TaskScheduler taskScheduler;
    private final ProxyRegistry proxyRegistry;
    private final RandomGenerator randomGenerator;

    /**
     * Invalidates all cached player data and reloads from scratch.
     * Clears console player, all platform players, and empties the cache.
     * Typically used during plugin reload or initialization.
     */
    public void invalidate() {
        // invalidate and load console FPlayer to reload name
        fPlayerRepository.invalid(getConsole().uuid());

        // invalidate all platform players
        platformPlayerAdapter.getOnlinePlayers().forEach(fPlayerRepository::invalid);

        // clear cache
        invalidateCache();
    }

    /**
     * Invalidates a specific player from all caches.
     *
     * @param uuid the UUID of the player to invalidate
     */
    public void invalidate(@NonNull UUID uuid) {
        fPlayerRepository.invalid(uuid);
    }

    /**
     * Clears all cached player data from both online and offline caches.
     * This operation removes all players from memory but does not affect the database.
     */
    public void invalidateCache() {
        fPlayerRepository.clearCache();
    }

    /**
     * Loads all online players from the database into the cache.
     */
    public void loadOnlineCache() {
        fPlayerRepository.getOnlinePlayersDatabase().forEach(this::addCache);
    }

    /**
     * Adds the console player to the cache with configured console name.
     * Creates a new console FPlayer if it doesn't exist, or ignores if already present.
     */
    public void addConsole() {
        FPlayer console = FPlayer.builder()
                .id(FPlayer.CONSOLE_ID)
                .name(fileFacade.config().logger().console())
                .type(FPlayer.CONSOLE_TYPE)
                .build();

        fPlayerRepository.saveOrIgnore(console);

        addCache(console);
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
    @NonNull
    public FPlayer saveOrUpdate(@NonNull UUID uuid, @NonNull String name, @Nullable String ip, boolean online) {
        return fPlayerRepository.saveOrUpdate(uuid, name, ip, online);
    }

    /**
     * Adds a player to the online cache.
     *
     * @param fPlayer the player to add to cache
     * @return the same player instance that was added
     */
    @NonNull
    public FPlayer addCache(@NonNull FPlayer fPlayer) {
        fPlayerRepository.add(fPlayer);
        return fPlayer;
    }

    /**
     * Updates an existing player in the cache.
     * Preserves online/offline status based on which cache the player is in.
     *
     * @param fPlayer the player data to update in cache
     * @return the same player instance that was updated
     */
    @NonNull
    public FPlayer updateCache(FPlayer fPlayer) {
        fPlayerRepository.updateCache(fPlayer);
        return fPlayer;
    }

    /**
     * Initializes all online platform players by loading their data and dispatching PlayerLoadEvent.
     * Players with cancelled events are invalidated from cache.
     *
     * @param reload whether this is a reload operation or initial startup
     */
    public void initialize(boolean reload) {
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

        // if no one was on the server, the cache may be invalid for other servers
        // because FlectonePulse on Proxy cannot send a message for servers that have no player
        if (proxyRegistry.hasEnabledProxy()) {
            taskScheduler.runAsyncTimer(() -> {
                // clears the cache of players who might have left from other servers
                if (platformPlayerAdapter.getOnlinePlayers().isEmpty()) {
                    invalidateCache();
                    addConsole();
                    loadOnlineCache();
                }
            }, 20L, 20L);
        }
    }

    /**
     * Removes a player from offline cache and optionally ensures online status for proxy players.
     * Fixes race condition where proxy might report player offline while they're actually online.
     *
     * @param uuid the UUID of the player to remove from offline cache
     * @param proxy whether this is called from proxy context (ensures online status if needed)
     */
    public void invalidateOfflineCache(@NonNull UUID uuid, boolean proxy) {
        fPlayerRepository.removeOffline(uuid);

        // idk why, but sometimes Proxy player offline, although he is already on the server.
        // I think that request that player is logged in is sent before request as player exits.
        // this is the only way to fix it
        if (proxy) {
            FPlayer fPlayer = fPlayerRepository.getFromDatabase(uuid);
            if (!fPlayer.isOnline()) {
                // update online cache
                fPlayer = updateCache(fPlayer.withOnline(true));

                // save to database
                fPlayerRepository.update(fPlayer);
            }
        }
    }

    /**
     * Removes a player from online cache.
     *
     * @param uuid the UUID of the player to remove from online cache
     */
    public void invalidateOnlineCache(@NonNull UUID uuid) {
        fPlayerRepository.removeOnline(uuid);
    }

    /**
     * Clears a player's online status and saves them to offline cache and database.
     * Sets online to false, removes from online cache, updates database, and adds to offline cache.
     *
     * @param fPlayer the player to clear and save as offline
     * @return the updated player with online=false
     */
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

    /**
     * Gets a player by database ID. Returns console player if ID is -1.
     *
     * @param id the database ID of the player
     * @return the player or console player if ID is -1
     */
    @NonNull
    public FPlayer getFPlayer(int id) {
        return fPlayerRepository.get(id);
    }

    /**
     * Gets the console player instance.
     *
     * @return the console FPlayer
     */
    @NonNull
    public FPlayer getConsole() {
        FPlayer fPlayer = getFPlayer(FEntity.UNKNOWN_UUID);
        if (!fPlayer.isConsole()) {
            return getFPlayer(FPlayer.CONSOLE_ID);
        }

        return fPlayer;
    }

    /**
     * Gets a player by name.
     *
     * @param name the player's name
     * @return the player or UNKNOWN if not found
     */
    @NonNull
    public FPlayer getFPlayer(@NonNull String name) {
        return fPlayerRepository.get(name);
    }

    /**
     * Gets a player by IP address.
     *
     * @param inetAddress the player's IP address
     * @return the player or UNKNOWN if not found
     */
    @NonNull
    public FPlayer getFPlayer(InetAddress inetAddress) {
        return fPlayerRepository.get(inetAddress);
    }

    /**
     * Gets a player by UUID.
     *
     * @param uuid the player's UUID
     * @return the player or UNKNOWN if not found
     */
    @NonNull
    public FPlayer getFPlayer(UUID uuid) {
        return fPlayerRepository.get(uuid);
    }

    /**
     * Gets a player from an FEntity by extracting its UUID.
     *
     * @param fEntity the entity to get the player for
     * @return the player associated with the entity's UUID
     */
    @NonNull
    public FPlayer getFPlayer(FEntity fEntity) {
        return getFPlayer(fEntity.uuid());
    }

    /**
     * Gets a player from a platform-specific player object (Bukkit, Fabric, etc.).
     * Handles console detection and creates temporary FPlayer for unknown players.
     *
     * @param platformPlayer the platform-specific player object
     * @return the FPlayer, console player, or a temporary player if not found
     */
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

    /**
     * Gets a random online player from platform players.
     *
     * @return a random FPlayer or UNKNOWN if no players are online
     */
    @NonNull
    public FPlayer getRandomFPlayer() {
        List<FPlayer> fPlayers = getPlatformFPlayers();
        if (fPlayers.isEmpty()) return FPlayer.UNKNOWN;

        int randomIndex = randomUtil.nextInt(0, fPlayers.size());
        return fPlayers.get(randomIndex);
    }

    /**
     * Gets all players from the database.
     *
     * @return list of all FPlayers in the database
     */
    @NonNull
    public List<FPlayer> findAllFPlayers() {
        return fPlayerRepository.getAllPlayersDatabase();
    }

    /**
     * Gets all online players from the cache.
     *
     * @return list of online FPlayers from cache
     */
    @NonNull
    public List<FPlayer> getOnlineFPlayers() {
        return fPlayerRepository.getOnlinePlayers();
    }

    /**
     * Gets all online players that are actually connected to the platform.
     * Filters cached online players by checking their actual platform online status.
     *
     * @return list of platform-verified online FPlayers
     */
    @NonNull
    public List<FPlayer> getPlatformFPlayers() {
        return fPlayerRepository.getOnlinePlayers().stream()
                .filter(platformPlayerAdapter::isOnline)
                .toList();
    }

    /**
     * Gets all online players including the console player.
     *
     * @return list of online FPlayers plus console
     */
    @NonNull
    public List<FPlayer> getFPlayersWithConsole() {
        return fPlayerRepository.getOnlineFPlayersWithConsole();
    }

}
