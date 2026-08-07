package net.flectone.pulse.data.repository;

import lombok.With;
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
 */
public interface SocialRepository {

    /**
     * Invalidates all cached data in the social repository.
     * Clears player color cache, settings cache, and ignore cache
     */
    void invalidate();

    /**
     * Loads ignore relationships for a player with cache support.
     * Returns cached ignores if available, otherwise loads from database and caches the result.
     *
     * @param fPlayer the player to load ignores for
     * @return list of ignore relationships for the player
     */
    List<Ignore> loadIgnores(FPlayer fPlayer);

    /**
     * Invalidates cached ignore relationships for a player.
     *
     * @param uuid the UUID of the player whose ignore cache should be cleared
     */
    void invalidateIgnores(UUID uuid);

    /**
     * Saves an ignore relationship between two players and updates the cache.
     *
     * @param fPlayer the player who is ignoring
     * @param fTarget the player being ignored
     * @return Optional containing the created ignore record, or empty if creation failed
     */
    Optional<Ignore> saveIgnore(FPlayer fPlayer, FPlayer fTarget);

    /**
     * Deletes an ignore relationship and updates the cache.
     *
     * @param fPlayer the player who was ignoring
     * @param ignore the ignore record to delete
     */
    void deleteIgnore(FPlayer fPlayer, Ignore ignore);

    /**
     * Gets all mail messages received by a player.
     *
     * @param fPlayer the player who received the mail messages
     * @return list of received mail messages
     */
    List<Mail> getReceiverMails(FPlayer fPlayer);

    /**
     * Gets all mail messages sent by a player.
     *
     * @param fPlayer the player who sent the mail messages
     * @return list of sent mail messages
     */
    List<Mail> getSenderMails(FPlayer fPlayer);

    /**
     * Saves a mail message from one player to another.
     *
     * @param fPlayer the sender of the mail message
     * @param fTarget the recipient of the mail message
     * @param message the content of the mail message
     * @return Optional containing the created mail record, or empty if creation failed
     */
    Optional<Mail> saveMail(FPlayer fPlayer, FPlayer fTarget, String message);

    /**
     * Deletes a mail message from the database.
     *
     * @param mail the mail record to delete
     */
    void deleteMail(Mail mail);

    /**
     * Loads color settings for a player with cache support.
     * Returns cached colors if available, otherwise loads from database and caches the result.
     *
     * @param fPlayer the player to load colors for
     * @return map of color types to sets of FColor objects
     */
    Map<FColor.Type, Set<FColor>> loadColors(@NonNull FPlayer fPlayer);

    /**
     * Invalidates cached color settings for a player.
     *
     * @param uuid the UUID of the player whose color cache should be cleared
     */
    void invalidateColors(UUID uuid);

    /**
     * Saves color settings for a player to the database and updates the cache.
     *
     * @param fPlayer the player whose colors are being saved
     * @param colors map of color types to sets of FColor objects to save
     */
    void saveColors(@NonNull FPlayer fPlayer, @NonNull Map<FColor.Type, Set<FColor>> colors);

    /**
     * Loads all settings for a player with cache support.
     * Returns cached settings if available, otherwise loads from database and caches the result.
     *
     * @param fPlayer the player to load settings for
     * @return Settings object containing boolean and text settings
     */
    Settings loadSettings(@NonNull FPlayer fPlayer);

    /**
     * Invalidates cached settings for a player.
     *
     * @param uuid the UUID of the player whose settings cache should be cleared
     */
    void invalidateSettings(UUID uuid);

    /**
     * Saves or updates a specific boolean setting for a player and updates the cache.
     *
     * @param fPlayer the player whose setting is being saved
     * @param setting the name of the boolean setting
     * @param value the boolean value to set
     */
    void saveOrUpdateSetting(@NonNull FPlayer fPlayer, @NonNull String setting, boolean value);

    /**
     * Saves or updates a specific text setting for a player and updates the cache.
     *
     * @param fPlayer the player whose setting is being saved
     * @param setting the SettingText enum representing the text setting type
     * @param value the text value to set, can be null to remove the setting
     */
    void saveOrUpdateSetting(@NonNull FPlayer fPlayer, @NonNull SettingText setting, @Nullable String value);

    /**
     * A player's stored chat settings, split by value type.
     *
     * @param booleans the on/off options
     * @param texts the text options such as the chosen locale
     */
    @With
    record Settings(
            Map<String, Boolean> booleans,
            Map<SettingText, String> texts
    ){

        /**
         * Settings for a player who has changed nothing.
         */
        public static final Settings EMPTY = new Settings(Map.of(), Map.of());

    }

}
