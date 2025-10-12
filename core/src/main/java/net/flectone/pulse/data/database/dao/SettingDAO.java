package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.SettingSQL;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SettingDAO extends BaseDAO<SettingSQL> {

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

    public void save(FPlayer player) {
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

    public void load(FPlayer player) {
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

    public void insertOrUpdate(FPlayer player, String setting) {
        useHandle(sql -> insertOrUpdate(sql, player, setting, player.getSetting(setting)));
    }

    public void insertOrUpdate(FPlayer player, SettingText settingText) {
        useHandle(sql -> insertOrUpdate(sql, player, settingText.name(), player.getSetting(settingText)));
    }
}