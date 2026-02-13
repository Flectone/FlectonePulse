package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.SettingSQL;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.logging.FLogger;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jspecify.annotations.NonNull;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Collections;
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
            player.settingsBoolean().forEach((messageType, value) ->
                    insertOrUpdate(sql, player, messageType, player.getSetting(messageType))
            );

            player.settingsText().forEach((settingText, string) ->
                    insertOrUpdate(sql, player, settingText.name(), string)
            );
        });
    }

    private void insertOrUpdate(SettingSQL sql, FPlayer player, String type, String value) {
        int updated = sql.update(player.id(), type, value);
        if (updated == 0) {
            try {
                sql.insert(player.id(), type, value);
            } catch (UnableToExecuteStatementException e) {
                if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                    sql.update(player.id(), type, value);
                } else throw e;
            }
        }
    }

    /**
     * Loads all settings for a player.
     *
     * @param player the player to load settings for
     * @return new FPlayer with settings
     */
    public FPlayer load(@NonNull FPlayer player) {
        int id = player.id();

        Map<String, Boolean> settingsBoolean = new Object2BooleanOpenHashMap<>();
        Map<SettingText, String> settingsText = new EnumMap<>(SettingText.class);

        withHandle(sql -> sql.findByPlayer(id)).forEach((key, value) -> {
            SettingText setting = SettingText.fromString(key);
            if (setting != null) {
                settingsText.put(setting, value);
                return;
            }

            try {
                settingsBoolean.put(key.toUpperCase(), "1".equals(value));
            } catch (IllegalArgumentException e) {
                fLogger.warning(e);
            }
        });

        return player.toBuilder()
                .settingsText(Collections.unmodifiableMap(settingsText))
                .settingsBoolean(Collections.unmodifiableMap(settingsBoolean))
                .build();
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