package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.repository.FPlayerRepository;
import net.flectone.pulse.data.repository.SocialRepository;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.generator.RandomGenerator;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.InetAddress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


/**
 * Central service for managing player data across the FlectonePulse plugin.
 * Provides methods for retrieving, caching, and updating player information including
 * settings, colors, ignore lists, and mail messages.
 * <p>
 * This service acts as a facade layer between the platform-specific player adapters
 * and the data repositories, handling cache management and data synchronization.
 * </p>
 * <p>
 * Players can be retrieved using various identifiers such as UUID, name, IP address,
 * database ID, or platform-specific player objects. The service maintains separate
 * caches for online and offline players to optimize performance.
 * </p>
 *
 * @see FPlayer
 * @see FPlayerRepository
 * @see SocialRepository
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
    private final SocialRepository socialRepository;
    private final IntegrationModule integrationModule;
    private final RandomGenerator randomUtil;
    private final EventDispatcher eventDispatcher;

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
        fPlayerRepository.clearCache();
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
     * Adds the console player to the cache with configured console name.
     * Creates a new console FPlayer if it doesn't exist, or ignores if already present.
     */
    public void addConsole() {
        FPlayer console = FPlayer.builder()
                .console(true)
                .name(fileFacade.config().logger().console())
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
     * Loads player settings with cache enabled by default.
     *
     * @param fPlayer the player to load settings for
     * @return the player with updated settings
     */
    @NonNull
    public FPlayer loadSettings(FPlayer fPlayer) {
        return loadSettings(fPlayer, true);
    }

    /**
     * Loads player settings with optional cache control.
     * Invalidates cached settings if cache parameter is false before loading.
     *
     * @param fPlayer the player to load settings for
     * @param cache whether to use cached settings or force reload from database
     * @return the player with updated settings
     */
    @NonNull
    public FPlayer loadSettings(FPlayer fPlayer, boolean cache) {
        if (!cache) {
            socialRepository.invalidateSettings(fPlayer.uuid());
        }

        return socialRepository.loadSettings(fPlayer);
    }

    /**
     * Loads player colors with cache enabled by default.
     *
     * @param fPlayer the player to load colors for
     * @return the player with updated colors
     */
    @NonNull
    public FPlayer loadColors(FPlayer fPlayer) {
        return loadColors(fPlayer, true);
    }

    /**
     * Loads player colors with optional cache control.
     * Invalidates cached colors if cache parameter is false before loading.
     *
     * @param fPlayer the player to load colors for
     * @param cache whether to use cached colors or force reload from database
     * @return the player with updated colors
     */
    @NonNull
    public FPlayer loadColors(FPlayer fPlayer, boolean cache) {
        if (!cache) {
            socialRepository.invalidateColors(fPlayer.uuid());
        }

        return socialRepository.loadColors(fPlayer);
    }

    /**
     * Loads player ignore list with cache enabled by default.
     *
     * @param fPlayer the player to load ignores for
     * @return the player with updated ignore list
     */
    @NonNull
    public FPlayer loadIgnores(FPlayer fPlayer) {
        return loadIgnores(fPlayer, true);
    }

    /**
     * Loads player ignore list with optional cache control.
     * Invalidates cached ignores if cache parameter is false before loading.
     *
     * @param fPlayer the player to load ignores for
     * @param cache whether to use cached ignores or force reload from database
     * @return the player with updated ignore list
     */
    @NonNull
    public FPlayer loadIgnores(FPlayer fPlayer, boolean cache) {
        if (!cache) {
            socialRepository.invalidateIgnores(fPlayer.uuid());
        }

        return socialRepository.loadIgnores(fPlayer);
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
        // load all platform players
        platformPlayerAdapter.getOnlinePlayers().forEach(uuid -> {
            FPlayer fPlayer = getFPlayer(uuid);
            PlayerLoadEvent playerLoadEvent = eventDispatcher.dispatch(new PlayerLoadEvent(fPlayer, reload));
            if (playerLoadEvent.cancelled()) {
                invalidate(uuid);
            }
        });
    }

    /**
     * Saves a text setting for a player and updates the cache.
     *
     * @param fPlayer the player to save the setting for
     * @param setting the setting type to save
     * @param value the text value to set, can be null
     * @return the updated player with the new setting
     */
    @NonNull
    public FPlayer saveSetting(@NonNull FPlayer fPlayer, SettingText setting, @Nullable String value) {
        fPlayer = fPlayer.withSetting(setting, value);
        socialRepository.saveOrUpdateSetting(fPlayer, setting);
        return updateCache(fPlayer);
    }

    /**
     * Saves a boolean setting for a player and updates the cache.
     *
     * @param fPlayer the player to save the setting for
     * @param setting the setting name to save
     * @param value the boolean value to set
     * @return the updated player with the new setting
     */
    @NonNull
    public FPlayer saveSetting(@NonNull FPlayer fPlayer, String setting, boolean value) {
        fPlayer = fPlayer.withSetting(setting, value);
        socialRepository.saveOrUpdateSetting(fPlayer, setting);
        return updateCache(fPlayer);
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
        if (id == -1) return getConsole();

        return fPlayerRepository.get(id);
    }

    /**
     * Gets the console player instance.
     *
     * @return the console FPlayer
     */
    @NonNull
    public FPlayer getConsole() {
        return getFPlayer(FEntity.UNKNOWN_UUID);
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

            return FPlayer.builder().name(name).build();
        }

        FPlayer fPlayer = getFPlayer(uuid);
        if (fPlayer.isUnknown()) {
            return FPlayer.builder()
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
     * Gets all online players from the database.
     *
     * @return list of online FPlayers from database
     */
    @NonNull
    public List<FPlayer> findOnlineFPlayers() {
        return fPlayerRepository.getOnlinePlayersDatabase();
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
     * Gets all online players visible to a specific viewer, filtering out vanished players.
     *
     * @param fViewer the player who is viewing
     * @return list of online players that the viewer can see
     */
    @NonNull
    public List<FPlayer> getVisibleFPlayersFor(FPlayer fViewer) {
        return getOnlineFPlayers()
                .stream()
                .filter(vanishedPlayer -> integrationModule.canSeeVanished(vanishedPlayer, fViewer))
                .toList();
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

    /**
     * Saves player colors and updates the cache.
     *
     * @param fPlayer the player with colors to save
     * @return the updated player from cache
     */
    @NonNull
    public FPlayer saveColors(@NonNull FPlayer fPlayer) {
        socialRepository.saveColors(fPlayer);
        return updateCache(fPlayer);
    }

    /**
     * Gets all mail messages received by a player.
     *
     * @param fPlayer the player to get mails for
     * @return list of mail messages received by the player
     */
    @NonNull
    public List<Mail> getReceiverMails(FPlayer fPlayer) {
        return socialRepository.getReceiverMails(fPlayer);
    }

    /**
     * Gets all mail messages sent by a player.
     *
     * @param fPlayer the player to get sent mails for
     * @return list of mail messages sent by the player
     */
    @NonNull
    public List<Mail> getSenderMails(FPlayer fPlayer) {
        return socialRepository.getSenderMails(fPlayer);
    }

    /**
     * Saves an ignore relationship between two players and updates the cache.
     *
     * @param fPlayer the player who is ignoring
     * @param fTarget the player being ignored
     * @return the updated player with the new ignore, or original player if ignore was null
     */
    @NonNull
    public FPlayer saveIgnore(@NonNull FPlayer fPlayer, @NonNull FPlayer fTarget) {
        Ignore ignore = socialRepository.saveAndGetIgnore(fPlayer, fTarget);
        if (ignore == null) return fPlayer;

        return updateCache(fPlayer.withIgnore(ignore));
    }

    /**
     * Saves a mail message from one player to another.
     *
     * @param fPlayer the sender of the mail
     * @param fTarget the recipient of the mail
     * @param message the mail message content
     * @return Optional containing the saved mail, or empty if save failed
     */
    @NonNull
    public Optional<Mail> saveMail(@NonNull FPlayer fPlayer, @NonNull FPlayer fTarget, @NonNull String message) {
        return socialRepository.saveAndGetMail(fPlayer, fTarget, message);
    }

    /**
     * Deletes an ignore relationship and updates the cache.
     *
     * @param fPlayer the player who was ignoring
     * @param ignore the ignore relationship to delete
     * @return the updated player without the ignore
     */
    @NonNull
    public FPlayer deleteIgnore(@NonNull FPlayer fPlayer, @NonNull Ignore ignore) {
        socialRepository.deleteIgnore(fPlayer, ignore);
        return updateCache(fPlayer.withoutIgnore(ignore));
    }

    /**
     * Deletes a mail message.
     *
     * @param mail the mail message to delete
     */
    public void deleteMail(@NonNull Mail mail) {
        socialRepository.deleteMail(mail);
    }

    /**
     * Updates a player's locale setting based on integration or provided locale.
     * Checks if the new locale differs from current setting before saving.
     *
     * @param fPlayer the player to update locale for
     * @param newLocale the new locale to set
     * @return true if locale was updated, false if unchanged or player is unknown
     */
    public boolean updateLocale(@NonNull FPlayer fPlayer, @NonNull String newLocale) {
        String locale = integrationModule.getTritonLocale(fPlayer);
        if (locale == null) {
            locale = newLocale;
        }

        SettingText settingName = SettingText.LOCALE;
        if (locale.equals(fPlayer.getSetting(settingName))) return false;
        if (fPlayer.isUnknown()) return false;

        saveSetting(fPlayer, settingName, locale);
        return true;
    }

}
