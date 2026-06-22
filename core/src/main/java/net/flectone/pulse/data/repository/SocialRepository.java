package net.flectone.pulse.data.repository;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import lombok.With;
import net.flectone.pulse.data.database.dao.FColorDao;
import net.flectone.pulse.data.database.dao.IgnoreDAO;
import net.flectone.pulse.data.database.dao.MailDAO;
import net.flectone.pulse.data.database.dao.SettingDAO;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.util.constant.SettingText;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Repository for managing social interactions and player data in FlectonePulse.
 * Handles ignore relationships, mail messages, color settings,
 * and player preferences with caching support.
 *
 * @author TheFaser
 * @since 0.8.1
 * @see IgnoreDAO
 * @see MailDAO
 * @see SettingDAO
 * @see FColorDao
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SocialRepository {

    private final @Named("playerColor") Cache<UUID, Map<FColor.Type, Set<FColor>>> playerColorCache;
    private final @Named("playerSetting") Cache<UUID, Settings> playerSettingCache;
    private final @Named("playerIgnore") Cache<UUID, List<Ignore>> playerIgnoreCache;

    private final IgnoreDAO ignoreDAO;
    private final MailDAO mailDAO;
    private final SettingDAO settingDAO;
    private final FColorDao fColorDao;

    /**
     * Loads ignore relationships for a player with cache support.
     * Returns cached ignores if available, otherwise loads from database and caches the result.
     *
     * @param fPlayer the player to load ignores for
     * @return list of ignore relationships for the player
     */
    public List<Ignore> loadIgnores(FPlayer fPlayer) {
        List<Ignore> cache = playerIgnoreCache.getIfPresent(fPlayer.uuid());
        if (cache != null) return cache;

        List<Ignore> ignores = ignoreDAO.load(fPlayer);
        playerIgnoreCache.put(fPlayer.uuid(), ignores);

        return ignores;
    }

    /**
     * Invalidates cached ignore relationships for a player.
     *
     * @param uuid the UUID of the player whose ignore cache should be cleared
     */
    public void invalidateIgnores(UUID uuid) {
        playerIgnoreCache.invalidate(uuid);
    }

    /**
     * Saves an ignore relationship between two players and updates the cache.
     *
     * @param fPlayer the player who is ignoring
     * @param fTarget the player being ignored
     * @return Optional containing the created ignore record, or empty if creation failed
     */
    public Optional<Ignore> saveIgnore(FPlayer fPlayer, FPlayer fTarget) {
        Ignore ignore = ignoreDAO.insert(fPlayer, fTarget);
        if (ignore == null) return Optional.empty();

        List<Ignore> ignores = new ArrayList<>(loadIgnores(fPlayer));
        ignores.add(ignore);

        playerIgnoreCache.put(fPlayer.uuid(), List.copyOf(ignores));

        return Optional.of(ignore);
    }

    /**
     * Deletes an ignore relationship and updates the cache.
     *
     * @param fPlayer the player who was ignoring
     * @param ignore the ignore record to delete
     */
    public void deleteIgnore(FPlayer fPlayer, Ignore ignore) {
        // invalidate record in database
        ignoreDAO.invalidate(ignore);

        // update cache
        List<Ignore> ignores = new ArrayList<>(loadIgnores(fPlayer));
        ignores.remove(ignore);

        playerIgnoreCache.put(fPlayer.uuid(), List.copyOf(ignores));
    }

    /**
     * Gets all mail messages received by a player.
     *
     * @param fPlayer the player who received the mail messages
     * @return list of received mail messages
     */
    public List<Mail> getReceiverMails(FPlayer fPlayer) {
        return mailDAO.getReceiver(fPlayer);
    }

    /**
     * Gets all mail messages sent by a player.
     *
     * @param fPlayer the player who sent the mail messages
     * @return list of sent mail messages
     */
    public List<Mail> getSenderMails(FPlayer fPlayer) {
        return mailDAO.getSender(fPlayer);
    }

    /**
     * Saves a mail message from one player to another.
     *
     * @param fPlayer the sender of the mail message
     * @param fTarget the recipient of the mail message
     * @param message the content of the mail message
     * @return Optional containing the created mail record, or empty if creation failed
     */
    @NonNull
    public Optional<Mail> saveMail(FPlayer fPlayer, FPlayer fTarget, String message) {
        return mailDAO.insert(fPlayer, fTarget, message);
    }

    /**
     * Deletes a mail message from the database.
     *
     * @param mail the mail record to delete
     */
    public void deleteMail(Mail mail) {
        mailDAO.delete(mail);
    }

    /**
     * Loads color settings for a player with cache support.
     * Returns cached colors if available, otherwise loads from database and caches the result.
     *
     * @param fPlayer the player to load colors for
     * @return map of color types to sets of FColor objects
     */
    @NonNull
    public Map<FColor.Type, Set<FColor>> loadColors(@NonNull FPlayer fPlayer) {
        Map<FColor.Type, Set<FColor>> cache = playerColorCache.getIfPresent(fPlayer.uuid());
        if (cache != null) return cache;

        Map<FColor.Type, Set<FColor>> colors = fColorDao.load(fPlayer);
        playerColorCache.put(fPlayer.uuid(), colors);

        return colors;
    }

    /**
     * Invalidates cached color settings for a player.
     *
     * @param uuid the UUID of the player whose color cache should be cleared
     */
    public void invalidateColors(UUID uuid) {
        playerColorCache.invalidate(uuid);
    }

    /**
     * Saves color settings for a player to the database and updates the cache.
     *
     * @param fPlayer the player whose colors are being saved
     * @param colors map of color types to sets of FColor objects to save
     */
    public void saveColors(@NonNull FPlayer fPlayer, @NonNull Map<FColor.Type, Set<FColor>> colors) {
        // save colors to database
        fColorDao.save(fPlayer, colors);

        // update cache
        playerColorCache.put(fPlayer.uuid(), colors);
    }

    /**
     * Loads all settings for a player with cache support.
     * Returns cached settings if available, otherwise loads from database and caches the result.
     *
     * @param fPlayer the player to load settings for
     * @return Settings object containing boolean and text settings
     */
    public Settings loadSettings(@NonNull FPlayer fPlayer) {
        Settings cache = playerSettingCache.getIfPresent(fPlayer.uuid());
        if (cache != null) return cache;

        Settings settings = settingDAO.load(fPlayer).orElse(Settings.EMPTY);
        playerSettingCache.put(fPlayer.uuid(), settings);

        return settings;
    }

    /**
     * Invalidates cached settings for a player.
     *
     * @param uuid the UUID of the player whose settings cache should be cleared
     */
    public void invalidateSettings(UUID uuid) {
        playerSettingCache.invalidate(uuid);
    }

    /**
     * Saves or updates a specific boolean setting for a player and updates the cache.
     *
     * @param fPlayer the player whose setting is being saved
     * @param setting the name of the boolean setting
     * @param value the boolean value to set
     */
    public void saveOrUpdateSetting(@NonNull FPlayer fPlayer, @NonNull String setting, boolean value) {
        // save setting to database
        settingDAO.insertOrUpdate(fPlayer, setting, value ? "1" : "0");

        Settings settings = loadSettings(fPlayer);

        Map<String, Boolean> newBooleans = new HashMap<>(settings.booleans());

        newBooleans.put(setting, value);

        settings = settings.withBooleans(Map.copyOf(newBooleans));

        playerSettingCache.put(fPlayer.uuid(), settings);
    }

    /**
     * Saves or updates a specific text setting for a player and updates the cache.
     *
     * @param fPlayer the player whose setting is being saved
     * @param setting the SettingText enum representing the text setting type
     * @param value the text value to set, can be null to remove the setting
     */
    public void saveOrUpdateSetting(@NonNull FPlayer fPlayer, @NonNull SettingText setting, @Nullable String value) {
        // save setting to database
        settingDAO.insertOrUpdate(fPlayer, setting.name(), value);

        Settings settings = loadSettings(fPlayer);

        Map<SettingText, String> newTexts = settings.texts().isEmpty()
                ? new EnumMap<>(SettingText.class)
                : new EnumMap<>(settings.texts());

        if (value == null) {
            newTexts.remove(setting);
        } else {
            newTexts.put(setting, value);
        }

        settings = settings.withTexts(Map.copyOf(newTexts));

        playerSettingCache.put(fPlayer.uuid(), settings);
    }

    @With
    public record Settings(
            Map<String, Boolean> booleans,
            Map<SettingText, String> texts
    ){

        public static final Settings EMPTY = new Settings(Map.of(), Map.of());

    }

}