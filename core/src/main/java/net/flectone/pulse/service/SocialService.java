package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.repository.SocialRepository;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

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
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SocialService {

    private final SocialRepository socialRepository;
    private final ProxyRegistry proxyRegistry;
    private final ProxySender proxySender;

    @Inject
    private Provider<FileFacade> fileFacadeProvider;

    @Inject
    private Provider<IntegrationModule> integrationModuleProvider;

    /**
     * Invalidates all cached social data for a player including colors, settings, and ignores.
     *
     * @param uuid the UUID of the player whose social data cache should be cleared
     */
    public void invalidate(UUID uuid) {
        socialRepository.invalidateColors(uuid);
        socialRepository.invalidateSettings(uuid);
        socialRepository.invalidateIgnores(uuid);
    }

    /**
     * Gets a player's setting value as a string by module name.
     *
     * @param fPlayer the player to get the setting for
     * @param moduleName the module name to retrieve the setting for
     * @return the setting value as a string
     */
    public @NonNull String getSetting(@NonNull FPlayer fPlayer, @NonNull ModuleName moduleName) {
        return getSetting(fPlayer, moduleName.name());
    }

    /**
     * Gets a player's text setting value by SettingText enum.
     *
     * @param fPlayer the player to get the setting for
     * @param settingText the SettingText enum representing the setting type
     * @return the text setting value, or null if not set
     */
    public @Nullable String getSetting(@NonNull FPlayer fPlayer, @Nullable SettingText settingText) {
        return loadSettings(fPlayer).texts().get(settingText);
    }

    /**
     * Gets a player's boolean setting value as a string ("1" or "0").
     *
     * @param fPlayer the player to get the setting for
     * @param moduleName the setting name to retrieve
     * @return "1" if the setting is true or not set, "0" if false
     */
    public @NonNull String getSetting(@NonNull FPlayer fPlayer, @Nullable String moduleName) {
        return isSetting(fPlayer, moduleName) ? "1" : "0";
    }

    /**
     * Checks if a player has a boolean setting enabled by module name.
     *
     * @param fPlayer the player to check the setting for
     * @param messageType the module name to check
     * @return true if the setting is enabled or not set, false if disabled
     */
    public boolean isSetting(@NonNull FPlayer fPlayer, @NonNull ModuleName messageType) {
        return isSetting(fPlayer, messageType.name());
    }

    /**
     * Checks if a player has a boolean setting enabled by name.
     *
     * @param fPlayer the player to check the setting for
     * @param moduleName the setting name to check
     * @return true if the setting is enabled or not set, false if disabled
     */
    public boolean isSetting(@NonNull FPlayer fPlayer, @Nullable String moduleName) {
        Boolean value = loadSettings(fPlayer).booleans().get(moduleName);
        return value == null || value;
    }

    /**
     * Saves a text setting for a player and notifies proxy if enabled.
     *
     * @param fPlayer the player to save the setting for
     * @param setting the SettingText enum representing the setting type
     * @param value the text value to set, can be null
     */
    public void saveSetting(@NonNull FPlayer fPlayer, @NonNull SettingText setting, @Nullable String value) {
        socialRepository.saveOrUpdateSetting(fPlayer, setting, value);

        if (proxyRegistry.hasEnabledProxy()) {
            proxySender.send(fPlayer, ModuleName.UPDATE_CACHE_SETTING);
        }
    }

    /**
     * Saves a boolean setting for a player and notifies proxy if enabled.
     *
     * @param fPlayer the player to save the setting for
     * @param setting the setting name to save
     * @param value the boolean value to set
     */
    public void saveSetting(@NonNull FPlayer fPlayer, @NonNull String setting, boolean value) {
        socialRepository.saveOrUpdateSetting(fPlayer, setting, value);

        if (proxyRegistry.hasEnabledProxy()) {
            proxySender.send(fPlayer, ModuleName.UPDATE_CACHE_SETTING);
        }
    }

    /**
     * Loads all settings for a player with caching enabled.
     *
     * @param fPlayer the player to load settings for
     * @return Settings object containing boolean and text settings
     */
    public SocialRepository.@NonNull Settings loadSettings(FPlayer fPlayer) {
        return loadSettings(fPlayer, true);
    }

    /**
     * Loads all settings for a player with optional cache control.
     *
     * @param fPlayer the player to load settings for
     * @param cache if true, use cached settings; if false, invalidate cache and reload from database
     * @return Settings object containing boolean and text settings
     */
    public SocialRepository.@NonNull Settings loadSettings(FPlayer fPlayer, boolean cache) {
        if (!cache) {
            socialRepository.invalidateSettings(fPlayer.uuid());
        }

        return socialRepository.loadSettings(fPlayer);
    }

    /**
     * Loads colors of a specific type for a player and converts them to a number-to-name map.
     *
     * @param fPlayer the player to load colors for
     * @param type the color type to load
     * @return map of color numbers to color names, empty if no colors found
     */
    @NonNull
    public Map<Integer, String> loadColors(@NonNull FPlayer fPlayer, FColor.@NonNull Type type) {
        Set<FColor> colors = loadColors(fPlayer).get(type);
        if (colors == null || colors.isEmpty()) return Map.of();

        Map<Integer, String> result = colors.stream()
                .collect(Collectors.toMap(
                        FColor::number,
                        FColor::name,
                        (v1, _) -> v1,
                        Int2ObjectArrayMap::new
                ));

        return Map.copyOf(result);
    }

    /**
     * Loads all colors for a player with caching enabled.
     *
     * @param fPlayer the player to load colors for
     * @return map of color types to sets of FColor objects
     */
    @NonNull
    public Map<FColor.Type, Set<FColor>> loadColors(FPlayer fPlayer) {
        return loadColors(fPlayer, true);
    }

    /**
     * Loads all colors for a player with optional cache control.
     *
     * @param fPlayer the player to load colors for
     * @param cache if true, use cached colors; if false, invalidate cache and reload from database
     * @return map of color types to sets of FColor objects
     */
    @NonNull
    public Map<FColor.Type, Set<FColor>> loadColors(FPlayer fPlayer, boolean cache) {
        if (!cache) {
            socialRepository.invalidateColors(fPlayer.uuid());
        }

        return socialRepository.loadColors(fPlayer);
    }

    /**
     * Saves colors of a specific type for a player, merging with existing colors.
     *
     * @param fPlayer the player to save colors for
     * @param type the color type to save
     * @param newColors the set of new colors to save, can be null or empty to clear
     */
    public void saveColors(@NonNull FPlayer fPlayer, FColor.@NonNull Type type, @Nullable Set<FColor> newColors) {
        Map<FColor.Type, Set<FColor>> fColors = loadColors(fPlayer);

        boolean newFColorsEmpty = newColors == null || newColors.isEmpty();
        boolean oldFColorsEmpty = fColors.isEmpty();
        if (newFColorsEmpty && oldFColorsEmpty) {
            saveColors(fPlayer, Map.of(type, Set.of()));
            return;
        }

        Map<FColor.Type, Set<FColor>> fColorMap = oldFColorsEmpty
                ? new EnumMap<>(FColor.Type.class)
                : new EnumMap<>(fColors);

        if (newFColorsEmpty) {
            fColorMap.put(type, Set.of());
        } else {
            fColorMap.put(type, Set.copyOf(newColors));
        }

        saveColors(fPlayer, Map.copyOf(fColorMap));
    }

    /**
     * Saves all colors for a player and notifies proxy if enabled.
     *
     * @param fPlayer the player to save colors for
     * @param colors map of color types to sets of FColor objects to save
     */
    public void saveColors(@NonNull FPlayer fPlayer, @NonNull Map<FColor.Type, Set<FColor>> colors) {
        socialRepository.saveColors(fPlayer, colors);

        if (proxyRegistry.hasEnabledProxy()) {
            proxySender.send(fPlayer, ModuleName.UPDATE_CACHE_COLOR);
        }
    }

    /**
     * Checks if a player is ignoring another player.
     *
     * @param fPlayer the player who might be ignoring
     * @param fTarget the potential target being ignored
     * @return true if fPlayer is ignoring fTarget, false otherwise
     */
    public boolean isIgnored(@NonNull FPlayer fPlayer, @NonNull FPlayer fTarget) {
        return loadIgnores(fPlayer).stream().anyMatch(ignore -> ignore.target() == fTarget.id());
    }

    /**
     * Loads all ignore relationships for a player with caching enabled.
     *
     * @param fPlayer the player to load ignores for
     * @return list of ignore relationships
     */
    @NonNull
    public List<Ignore> loadIgnores(FPlayer fPlayer) {
        return loadIgnores(fPlayer, true);
    }

    /**
     * Loads all ignore relationships for a player with optional cache control.
     *
     * @param fPlayer the player to load ignores for
     * @param cache if true, use cached ignores; if false, invalidate cache and reload from database
     * @return list of ignore relationships
     */
    @NonNull
    public List<Ignore> loadIgnores(FPlayer fPlayer, boolean cache) {
        if (!cache) {
            socialRepository.invalidateIgnores(fPlayer.uuid());
        }

        return socialRepository.loadIgnores(fPlayer);
    }

    /**
     * Gets all mail messages received by a player.
     *
     * @param fPlayer the player who received the mail messages
     * @return list of received mail messages
     */
    @NonNull
    public List<Mail> getReceiverMails(FPlayer fPlayer) {
        return socialRepository.getReceiverMails(fPlayer);
    }

    /**
     * Gets all mail messages sent by a player.
     *
     * @param fPlayer the player who sent the mail messages
     * @return list of sent mail messages
     */
    @NonNull
    public List<Mail> getSenderMails(FPlayer fPlayer) {
        return socialRepository.getSenderMails(fPlayer);
    }

    /**
     * Saves an ignore relationship between two players and notifies proxy if enabled.
     *
     * @param fPlayer the player who is ignoring
     * @param fTarget the player being ignored
     * @return Optional containing the created ignore record, or empty if creation failed
     */
    @NonNull
    public Optional<Ignore> saveIgnore(@NonNull FPlayer fPlayer, @NonNull FPlayer fTarget) {
        Optional<Ignore> ignore = socialRepository.saveIgnore(fPlayer, fTarget);
        if (ignore.isEmpty()) return Optional.empty();

        if (proxyRegistry.hasEnabledProxy()) {
            proxySender.send(fPlayer, ModuleName.UPDATE_CACHE_IGNORE);
        }

        return ignore;
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
    public Optional<Mail> saveMail(@NonNull FPlayer fPlayer, @NonNull FPlayer fTarget, @NonNull String message) {
        return socialRepository.saveMail(fPlayer, fTarget, message);
    }

    /**
     * Deletes an ignore relationship and notifies proxy if enabled.
     *
     * @param fPlayer the player who was ignoring
     * @param ignore the ignore record to delete
     */
    public void deleteIgnore(@NonNull FPlayer fPlayer, @NonNull Ignore ignore) {
        socialRepository.deleteIgnore(fPlayer, ignore);

        if (proxyRegistry.hasEnabledProxy()) {
            proxySender.send(fPlayer, ModuleName.UPDATE_CACHE_IGNORE);
        }
    }

    /**
     * Deletes a mail message from the database.
     *
     * @param mail the mail record to delete
     */
    public void deleteMail(@NonNull Mail mail) {
        socialRepository.deleteMail(mail);
    }

    /**
     * Updates a player's locale setting based on Triton integration or provided value.
     * Only updates if the locale has changed and the player is not unknown.
     *
     * @param fPlayer the player whose locale is being updated
     * @param newLocale the new locale to set if Triton locale is unavailable
     * @return true if the locale was updated, false if unchanged or player is unknown
     */
    public boolean updateLocale(@NonNull FPlayer fPlayer, @NonNull String newLocale) {
        String locale = integrationModuleProvider.get().getTritonLocale(fPlayer);
        if (locale == null) {
            locale = newLocale;
        }

        SettingText settingName = SettingText.LOCALE;
        if (locale.equals(getSetting(fPlayer, settingName))) return false;
        if (fPlayer.isUnknown()) return false;

        saveSetting(fPlayer, settingName, locale);
        return true;
    }

    /**
     * Checks whether a given entity is currently in vanish mode (invisible to other players).
     * <p>
     * This method first checks if the entity is a player with a configured vanish status setting.
     * If not found locally, it delegates to the integration module to check external vanish providers.
     *
     * @param fEntity the entity to check for vanish status
     * @return true if the entity is vanished, false otherwise
     */
    public boolean isVanished(@NonNull FEntity fEntity) {
        if (fEntity instanceof FPlayer fPlayer
                && fileFacadeProvider.get().integration().supervanish().proxySync()
                && getSetting(fPlayer, SettingText.VANISH_STATUS) != null) {
            return true;
        }

        return integrationModuleProvider.get().isVanished(fEntity);
    }

    /**
     * Determines whether a viewer can see a target entity that may be in vanish mode.
     * <p>
     * This is a convenience overload that automatically checks if the target is vanished.
     *
     * @param fTarget the target entity that might be vanished, must not be null
     * @param fViewer the viewer entity attempting to see the target, must not be null
     * @return true if the viewer can see the target, false if the target is vanished and the viewer lacks permission
     */
    public boolean canSeeVanished(@NonNull FEntity fTarget, @NonNull FEntity fViewer) {
        return canSeeVanished(fTarget, fViewer, isVanished(fTarget));
    }

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
    public boolean canSeeVanished(@NonNull FEntity fTarget, @NonNull FEntity fViewer, boolean targetVanished) {
        if (fTarget.equals(fViewer)) return true;
        if (fViewer instanceof FPlayer fPlayer && fPlayer.isConsole()) return true;

        return !targetVanished || integrationModuleProvider.get().hasSeeVanishPermission(fViewer);
    }

}
