package net.flectone.pulse.service;

import net.flectone.pulse.persistence.repository.SocialRepository;
import net.flectone.pulse.model.value.FColor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Service for managing social interactions and player settings in FlectonePulse.
 * Handles player preferences including settings, colors, ignore lists, and mail messages.
 * Integrates with proxy systems to synchronize social data across servers.
 *
 * @see SocialRepository
 * @see FPlayer
 *
 * @author TheFaser
 * @since 1.10.1
 */
public interface SocialService {

    /**
     * Invalidates all cached data in the social repository.
     * Clears player colors, settings and ignores
     */
    void invalidate();

    /**
     * Invalidates all cached social data for a player including colors, settings, and ignores.
     *
     * @param uuid the UUID of the player whose social data cache should be cleared
     */
    void invalidate(UUID uuid);

    /**
     * Gets a player's setting value as a string by module name.
     *
     * @param fPlayer the player to get the setting for
     * @param moduleName the module name to retrieve the setting for
     * @return the setting value as a string
     */
    @NonNull String getSetting(@NonNull FPlayer fPlayer, @NonNull ModuleName moduleName);

    /**
     * Gets a player's text setting value by SettingText enum.
     *
     * @param fPlayer the player to get the setting for
     * @param settingText the SettingText enum representing the setting type
     * @return the text setting value, or null if not set
     */
    @Nullable String getSetting(@NonNull FPlayer fPlayer, @Nullable SettingText settingText);

    /**
     * Gets a player's boolean setting value as a string ("1" or "0").
     *
     * @param fPlayer the player to get the setting for
     * @param moduleName the setting name to retrieve
     * @return "1" if the setting is true or not set, "0" if false
     */
    @NonNull String getSetting(@NonNull FPlayer fPlayer, @Nullable String moduleName);

    /**
     * Checks if a player has a boolean setting enabled by module name.
     *
     * @param fPlayer the player to check the setting for
     * @param messageType the module name to check
     * @return true if the setting is enabled or not set, false if disabled
     */
    boolean isSetting(@NonNull FPlayer fPlayer, @NonNull ModuleName messageType);

    /**
     * Checks if a player has a boolean setting enabled by name.
     *
     * @param fPlayer the player to check the setting for
     * @param moduleName the setting name to check
     * @return true if the setting is enabled or not set, false if disabled
     */
    boolean isSetting(@NonNull FPlayer fPlayer, @Nullable String moduleName);

    /**
     * Saves a text setting for a player and notifies proxy if enabled.
     *
     * @param fPlayer the player to save the setting for
     * @param setting the SettingText enum representing the setting type
     * @param value the text value to set, can be null
     */
    void saveSetting(@NonNull FPlayer fPlayer, @NonNull SettingText setting, @Nullable String value);

    /**
     * Saves a boolean setting for a player and notifies proxy if enabled.
     *
     * @param fPlayer the player to save the setting for
     * @param setting the setting name to save
     * @param value the boolean value to set
     */
    void saveSetting(@NonNull FPlayer fPlayer, @NonNull String setting, boolean value);

    /**
     * Loads all settings for a player with caching enabled.
     *
     * @param fPlayer the player to load settings for
     * @return Settings object containing boolean and text settings
     */
    SocialRepository.@NonNull Settings loadSettings(FPlayer fPlayer);

    /**
     * Loads all settings for a player with optional cache control.
     *
     * @param fPlayer the player to load settings for
     * @param cache if true, use cached settings; if false, invalidate cache and reload from database
     * @return Settings object containing boolean and text settings
     */
    SocialRepository.@NonNull Settings loadSettings(FPlayer fPlayer, boolean cache);

    /**
     * Loads colors of a specific type for a player and converts them to a number-to-name map.
     *
     * @param fPlayer the player to load colors for
     * @param type the color type to load
     * @return map of color numbers to color names, empty if no colors found
     */
    @NonNull
    Map<Integer, String> loadColors(@NonNull FPlayer fPlayer, FColor.@NonNull Type type);

    /**
     * Loads all colors for a player with caching enabled.
     *
     * @param fPlayer the player to load colors for
     * @return map of color types to sets of FColor objects
     */
    @NonNull
    Map<FColor.Type, Set<FColor>> loadColors(FPlayer fPlayer);

    /**
     * Loads all colors for a player with optional cache control.
     *
     * @param fPlayer the player to load colors for
     * @param cache if true, use cached colors; if false, invalidate cache and reload from database
     * @return map of color types to sets of FColor objects
     */
    @NonNull
    Map<FColor.Type, Set<FColor>> loadColors(FPlayer fPlayer, boolean cache);

    /**
     * Saves colors of a specific type for a player, merging with existing colors.
     *
     * @param fPlayer the player to save colors for
     * @param type the color type to save
     * @param newColors the set of new colors to save, can be null or empty to clear
     */
    void saveColors(@NonNull FPlayer fPlayer, FColor.@NonNull Type type, @Nullable Set<FColor> newColors);

    /**
     * Saves all colors for a player and notifies proxy if enabled.
     *
     * @param fPlayer the player to save colors for
     * @param colors map of color types to sets of FColor objects to save
     */
    void saveColors(@NonNull FPlayer fPlayer, @NonNull Map<FColor.Type, Set<FColor>> colors);

    /**
     * Checks if a player is ignoring another player.
     *
     * @param fPlayer the player who might be ignoring
     * @param fTarget the potential target being ignored
     * @return true if fPlayer is ignoring fTarget, false otherwise
     */
    boolean isIgnored(@NonNull FPlayer fPlayer, @NonNull FPlayer fTarget);

    /**
     * Loads all ignore relationships for a player with caching enabled.
     *
     * @param fPlayer the player to load ignores for
     * @return list of ignore relationships
     */
    @NonNull
    List<Ignore> loadIgnores(FPlayer fPlayer);

    /**
     * Loads all ignore relationships for a player with optional cache control.
     *
     * @param fPlayer the player to load ignores for
     * @param cache if true, use cached ignores; if false, invalidate cache and reload from database
     * @return list of ignore relationships
     */
    @NonNull
    List<Ignore> loadIgnores(FPlayer fPlayer, boolean cache);

    /**
     * Gets all mail messages received by a player.
     *
     * @param fPlayer the player who received the mail messages
     * @return list of received mail messages
     */
    @NonNull
    List<Mail> getReceiverMails(FPlayer fPlayer);

    /**
     * Gets all mail messages sent by a player.
     *
     * @param fPlayer the player who sent the mail messages
     * @return list of sent mail messages
     */
    @NonNull
    List<Mail> getSenderMails(FPlayer fPlayer);

    /**
     * Saves an ignore relationship between two players and notifies proxy if enabled.
     *
     * @param fPlayer the player who is ignoring
     * @param fTarget the player being ignored
     * @return Optional containing the created ignore record, or empty if creation failed
     */
    @NonNull
    Optional<Ignore> saveIgnore(@NonNull FPlayer fPlayer, @NonNull FPlayer fTarget);

    /**
     * Saves a mail message from one player to another.
     *
     * @param fPlayer the sender of the mail message
     * @param fTarget the recipient of the mail message
     * @param message the content of the mail message
     * @return Optional containing the created mail record, or empty if creation failed
     */
    @NonNull
    Optional<Mail> saveMail(@NonNull FPlayer fPlayer, @NonNull FPlayer fTarget, @NonNull String message);

    /**
     * Deletes an ignore relationship and notifies proxy if enabled.
     *
     * @param fPlayer the player who was ignoring
     * @param ignore the ignore record to delete
     */
    void deleteIgnore(@NonNull FPlayer fPlayer, @NonNull Ignore ignore);

    /**
     * Deletes a mail message from the database.
     *
     * @param mail the mail record to delete
     */
    void deleteMail(@NonNull Mail mail);

    /**
     * Updates a player's locale setting based on Triton integration or provided value.
     * Only updates if the locale has changed and the player is not unknown.
     *
     * @param fPlayer the player whose locale is being updated
     * @param newLocale the new locale to set if Triton locale is unavailable
     * @return true if the locale was updated, false if unchanged or player is unknown
     */
    boolean updateLocale(@NonNull FPlayer fPlayer, @NonNull String newLocale);

    /**
     * Checks whether a given entity is currently in vanish mode (invisible to other players).
     * <p>
     * This method first checks if the entity is a player with a configured vanish status setting.
     * If not found locally, it delegates to the integration module to check external vanish providers.
     *
     * @param fEntity the entity to check for vanish status
     * @return true if the entity is vanished, false otherwise
     */
    boolean isVanished(@NonNull FEntity fEntity);

    /**
     * Checks whether a given entity is currently in vanish mode (invisible to other players).
     * <p>
     * This method first checks if the entity is a player with a configured vanish status setting.
     * If not found locally, it delegates to the integration module to check external vanish providers.
     *
     * @param fEntity the entity to check for vanish status
     * @param checkVanishIntegration whether to enforce that a vanish integration is present; if true and no integration is available, returns false immediately
     * @return true if the entity is vanished, false otherwise
     */
    boolean isVanished(@NonNull FEntity fEntity, boolean checkVanishIntegration);

    /**
     * Determines whether a viewer can see a target entity that may be in vanish mode.
     * <p>
     * This is a convenience overload that automatically checks if the target is vanished.
     *
     * @param fTarget the target entity that might be vanished, must not be null
     * @param fViewer the viewer entity attempting to see the target, must not be null
     * @return true if the viewer can see the target, false if the target is vanished and the viewer lacks permission
     */
    boolean canSeeVanished(@NonNull FEntity fTarget, @NonNull FEntity fViewer);

    /**
     * Determines whether a viewer can see a target entity with a known vanish status.
     * <p>
     * The following rules apply:
     * <ul>
     *   <li>An entity can always see itself</li>
     *   <li>Console entities can always see vanished players</li>
     *   <li>If the target is not vanished, any viewer can see them</li>
     *   <li>If the target is vanished, only viewers with the appropriate permission can see them</li>
     * </ul>
     *
     * @param fTarget the target entity that might be vanished, must not be null
     * @param fViewer the viewer entity attempting to see the target, must not be null
     * @param targetVanished pre-computed vanish status of the target entity
     * @return true if the viewer can see the target, false if the target is vanished and the viewer lacks permission
     */
    boolean canSeeVanished(@NonNull FEntity fTarget, @NonNull FEntity fViewer, boolean targetVanished);

}
