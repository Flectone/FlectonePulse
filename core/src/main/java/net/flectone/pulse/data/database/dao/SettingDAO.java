package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.SettingSQL;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.logging.FLogger;
import org.jspecify.annotations.NonNull;

/**
 * Data Access Object for player settings in FlectonePulse.
 * Handles saving and loading player preferences and configurations.
 *
 * @author TheFaser
 * @since 1.6.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SettingDAO implements BaseDAO<SettingSQL> {

    private final Database database;
    private final FLogger fLogger;

    @Override
    public Database database() {
        return database;
    }

    @Override
    public Class<SettingSQL> sqlClass() {
        return SettingSQL.class;
    }

    /**
     * Saves all settings for a player.
     *
     * @param player the player to save settings for
     */
    public void save(@NonNull FPlayer player) {
        useTransaction(sql -> {
            player.getSettingsBoolean().forEach((messageType, value) ->
                    insertOrUpdate(sql, player, messageType, player.getSetting(messageType))
            );

            player.getSettingsText().forEach((settingText, string) ->
                    insertOrUpdate(sql, player, settingText.name(), string)
            );
        });
    }

    private void insertOrUpdate(SettingSQL sql, FPlayer player, String type, String value) {
        int updated = sql.update(player.getId(), type, value);
        if (updated == 0) {
            sql.insert(player.getId(), type, value);
        }
    }

    /**
     * Loads all settings for a player.
     *
     * @param player the player to load settings for
     */
    public void load(@NonNull FPlayer player) {
        player.getSettingsBoolean().clear();
        player.getSettingsText().clear();

        useHandle(sql -> sql.findByPlayer(player.getId()).forEach((string, value) -> {
            SettingText settingText = SettingText.fromString(string);
            if (settingText == null) {
                try {
                    player.setSetting(string.toUpperCase(), value.equals("1"));
                } catch (IllegalArgumentException e) {
                    fLogger.warning(e);
                }
            } else {
                player.setSetting(settingText, value);
            }
        }));
    }

    /**
     * Inserts or updates a specific boolean setting for a player.
     *
     * @param player the player
     * @param setting the setting name
     */
    public void insertOrUpdate(@NonNull FPlayer player, @NonNull String setting) {
        useHandle(sql -> insertOrUpdate(sql, player, setting, player.getSetting(setting)));
    }

    /**
     * Inserts or updates a specific text setting for a player.
     *
     * @param player the player
     * @param settingText the setting text type
     */
    public void insertOrUpdate(@NonNull FPlayer player, SettingText settingText) {
        useHandle(sql -> insertOrUpdate(sql, player, settingText.name(), player.getSetting(settingText)));
    }

}