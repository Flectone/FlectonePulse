package net.flectone.pulse.data.repository;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.dao.*;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.PlayTime;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.util.constant.SettingText;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Repository for managing social interactions in FlectonePulse.
 * Handles ignore relationships and mail messages between players.
 *
 * @author TheFaser
 * @since 0.8.1
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SocialRepository {

    private final @Named("playtime") Cache<UUID, PlayTime> playTimeCache;
    private final @Named("playerColor") Cache<UUID, Map<FColor.Type, Set<FColor>>> playerColorCache;
    private final @Named("playerSetting") Cache<UUID, Settings> playerSettingCache;
    private final @Named("playerIgnore") Cache<UUID, List<Ignore>> playerIgnoreCache;

    private final IgnoreDAO ignoreDAO;
    private final MailDAO mailDAO;
    private final TimeDAO timeDAO;
    private final SettingDAO settingDAO;
    private final FColorDao fColorDao;

    /**
     * Loads ignore relationships for a player with cache support.
     * Returns cached ignores if available, otherwise loads from database and caches the result.
     *
     * @param fPlayer the player to load ignores for
     * @return new FPlayer with ignores loaded
     */
    public FPlayer loadIgnores(FPlayer fPlayer) {
        List<Ignore> cache = playerIgnoreCache.getIfPresent(fPlayer.uuid());
        if (cache != null) return fPlayer.withIgnores(cache);

        fPlayer = ignoreDAO.load(fPlayer);
        playerIgnoreCache.put(fPlayer.uuid(), fPlayer.ignores());

        return fPlayer;
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
     * Saves an ignore relationship between two players and returns the created record.
     * Invalidates the ignore cache before saving to ensure fresh data on next load.
     *
     * @param fPlayer the player who is ignoring
     * @param fTarget the player being ignored
     * @return the created ignore record, or null if players are unknown
     */
    public @Nullable Ignore saveAndGetIgnore(FPlayer fPlayer, FPlayer fTarget) {
        invalidateIgnores(fPlayer.uuid());

        // return new ignore
        return ignoreDAO.insert(fPlayer, fTarget);
    }

    /**
     * Deletes an ignore relationship and invalidates the cache.
     *
     * @param fPlayer the player who was ignoring
     * @param ignore the ignore record to delete
     */
    public void deleteIgnore(FPlayer fPlayer, Ignore ignore) {
        invalidateIgnores(fPlayer.uuid());

        // invalidate record in database
        ignoreDAO.invalidate(ignore);
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
     * Saves a mail message from one player to another and returns the created record.
     *
     * @param fPlayer the sender of the mail message
     * @param fTarget the recipient of the mail message
     * @param message the content of the mail message
     * @return Optional containing the created mail record, or empty if creation failed
     */
    @NonNull
    public Optional<Mail> saveAndGetMail(FPlayer fPlayer, FPlayer fTarget, String message) {
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
     * Saves a player's join session when they connect to the server.
     *
     * @param fPlayer the player whose join session is being saved
     */
    public void saveJoinSession(FPlayer fPlayer) {
        timeDAO.saveJoin(fPlayer);
    }

    /**
     * Saves a playtime session directly.
     *
     * @param playTime the playtime session to save
     */
    public void saveJoinSession(PlayTime playTime) {
        timeDAO.saveSession(playTime);
    }

    /**
     * Saves a player's AFK session status change.
     *
     * @param fPlayer the player whose AFK status is being updated
     * @param afk true if the player is going AFK, false if returning from AFK
     */
    public void saveAfkSession(FPlayer fPlayer, boolean afk) {
        timeDAO.saveAfk(fPlayer, afk, getPlayTime(fPlayer));
    }

    /**
     * Saves a player's last seen timestamp when they disconnect from the server.
     *
     * @param fPlayer the player whose last seen time is being recorded
     */
    public void saveLastSeen(FPlayer fPlayer) {
        timeDAO.saveQuit(fPlayer, getPlayTime(fPlayer));
    }

    /**
     * Gets the playtime statistics for a specific player with cache support.
     * Returns cached playtime if available, otherwise loads from database and caches the result.
     *
     * @param fPlayer the player to get playtime statistics for
     * @return the player's playtime statistics, or null if not found
     */
    public @Nullable PlayTime getPlayTime(FPlayer fPlayer) {
        PlayTime cached = playTimeCache.getIfPresent(fPlayer.uuid());
        if (cached != null) return cached;

        Optional<PlayTime> playTime = timeDAO.getByPlayer(fPlayer);
        playTime.ifPresent(time -> playTimeCache.put(fPlayer.uuid(), time));

        return playTime.orElse(null);
    }

    /**
     * Gets the total count of all playtime records in the database.
     *
     * @return the total number of playtime records
     */
    public int getPlayTimesCount() {
        return timeDAO.getTotalCount();
    }

    /**
     * Gets a paginated list of all playtime records from the database.
     *
     * @param limit the maximum number of records to retrieve
     * @param offset the number of records to skip before returning results
     * @return list of playtime records within the specified range
     */
    public List<PlayTime> getAllPlayTimes(int limit, int offset) {
        return timeDAO.getAllPlayTimes(limit, offset);
    }

    /**
     * Invalidates cached playtime statistics for a player.
     *
     * @param uuid the UUID of the player whose playtime cache should be cleared
     */
    public void invalidatePlaytime(UUID uuid) {
        playTimeCache.invalidate(uuid);
    }

    /**
     * Loads color settings for a player with cache support.
     * Returns cached colors if available, otherwise loads from database and caches the result.
     *
     * @param fPlayer the player to load colors for
     * @return new FPlayer with colors loaded
     */
    public FPlayer loadColors(@NonNull FPlayer fPlayer) {
        Map<FColor.Type, Set<FColor>> cache = playerColorCache.getIfPresent(fPlayer.uuid());
        if (cache != null) return fPlayer.withFColors(cache);

        fPlayer = fColorDao.load(fPlayer);
        playerColorCache.put(fPlayer.uuid(), fPlayer.fColors());

        return fPlayer;
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
     * Saves color settings for a player to the database.
     * Invalidates the color cache before saving to ensure fresh data on next load.
     *
     * @param fPlayer the player whose colors are being saved
     */
    public void saveColors(@NonNull FPlayer fPlayer) {
        invalidateColors(fPlayer.uuid());

        // save colors to database
        fColorDao.save(fPlayer);
    }

    /**
     * Loads all settings for a player with cache support.
     * Returns cached settings if available, otherwise loads from database and caches the result.
     *
     * @param fPlayer the player to load settings for
     * @return new FPlayer with settings loaded
     */
    public FPlayer loadSettings(@NonNull FPlayer fPlayer) {
        Settings cache = playerSettingCache.getIfPresent(fPlayer.uuid());
        if (cache != null) {
            return fPlayer.toBuilder()
                    .settingsBoolean(cache.booleans())
                    .settingsText(cache.texts())
                    .build();
        }

        fPlayer = settingDAO.load(fPlayer);
        playerSettingCache.put(fPlayer.uuid(), new Settings(fPlayer.settingsBoolean(), fPlayer.settingsText()));

        return fPlayer;
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
     * Saves all settings for a player to the database.
     * Invalidates the settings cache before saving to ensure fresh data on next load.
     *
     * @param fPlayer the player whose settings are being saved
     */
    public void saveSettings(@NonNull FPlayer fPlayer) {
        invalidateSettings(fPlayer.uuid());

        // save settings to database
        settingDAO.save(fPlayer);
    }

    /**
     * Saves or updates a specific boolean setting for a player.
     * Invalidates the settings cache before saving to ensure fresh data on next load.
     *
     * @param fPlayer the player whose setting is being saved
     * @param setting the name of the boolean setting
     */
    public void saveOrUpdateSetting(@NonNull FPlayer fPlayer, @NonNull String setting) {
        invalidateSettings(fPlayer.uuid());

        // save setting to database
        settingDAO.insertOrUpdate(fPlayer, setting);
    }

    /**
     * Saves or updates a specific text setting for a player.
     * Invalidates the settings cache before saving to ensure fresh data on next load.
     *
     * @param fPlayer the player whose setting is being saved
     * @param setting the SettingText enum representing the text setting type
     */
    public void saveOrUpdateSetting(@NonNull FPlayer fPlayer, @NonNull SettingText setting) {
        invalidateSettings(fPlayer.uuid());

        // save setting to database
        settingDAO.insertOrUpdate(fPlayer, setting);
    }

    public record Settings(
            Map<String, Boolean> booleans,
            Map<SettingText, String> texts
    ){}

}