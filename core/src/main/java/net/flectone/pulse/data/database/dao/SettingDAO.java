package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.setting.*;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.constant.SettingText;
import org.jspecify.annotations.NonNull;

import java.util.EnumMap;
import java.util.Map;

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

    @Override
    public Database database() {
        return database;
    }

    @Override
    public Class<? extends SettingSQL> sqlClass() {
        return switch (database.config().type()) {
            case H2 -> SettingH2.class;
            case MARIADB -> SettingMariaDB.class;
            case MYSQL -> SettingMySQL.class;
            case POSTGRESQL -> SettingPostgreSQL.class;
            case SQLITE -> SettingSQLite.class;
        };
    }

    /**
     * Saves all settings for a player.
     *
     * @param player the player to save settings for
     */
    public void save(@NonNull FPlayer player) {
        if (database.isClosed()) return;

        useTransaction(sql -> {
            player.settingsBoolean().forEach((messageType, _) ->
                    insertOrUpdate(sql, player, messageType, player.getSetting(messageType))
            );

            player.settingsText().forEach((settingText, string) ->
                    insertOrUpdate(sql, player, settingText.name(), string)
            );
        });
    }

    private void insertOrUpdate(SettingSQL sql, FPlayer player, String type, String value) {
        sql.upsert(player.id(), type, value);
    }

    /**
     * Loads all settings for a player.
     *
     * @param player the player to load settings for
     * @return new FPlayer with settings
     */
    public FPlayer load(@NonNull FPlayer player) {
        if (database.isClosed()) return player;

        int id = player.id();

        Map<String, Boolean> settingsBoolean = new Object2BooleanOpenHashMap<>();
        Map<SettingText, String> settingsText = new EnumMap<>(SettingText.class);

        withHandle(sql -> sql.findByPlayer(id)).forEach((key, value) -> {
            if (value == null) return;

            SettingText setting = SettingText.fromString(key);
            if (setting != null) {
                settingsText.put(setting, value);
                return;
            }

            settingsBoolean.put(key.toUpperCase(), "1".equals(value));
        });

        return player.toBuilder()
                .settingsText(Map.copyOf(settingsText))
                .settingsBoolean(Map.copyOf(settingsBoolean))
                .build();
    }

    /**
     * Inserts or updates a specific boolean setting for a player.
     *
     * @param player the player
     * @param setting the setting name
     */
    public void insertOrUpdate(@NonNull FPlayer player, @NonNull String setting) {
        if (database.isClosed()) return;

        useHandle(sql -> insertOrUpdate(sql, player, setting, player.getSetting(setting)));
    }

    /**
     * Inserts or updates a specific text setting for a player.
     *
     * @param player the player
     * @param settingText the setting text type
     */
    public void insertOrUpdate(@NonNull FPlayer player, SettingText settingText) {
        if (database.isClosed()) return;

        useHandle(sql -> insertOrUpdate(sql, player, settingText.name(), player.getSetting(settingText)));
    }

}