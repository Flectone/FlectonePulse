package net.flectone.pulse.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.database.sql.SettingSQL;
import net.flectone.pulse.model.FPlayer;

import java.util.Optional;

@Singleton
public class SettingDAO extends BaseDAO<SettingSQL> {

    @Inject
    public SettingDAO(Database database) {
        super(database, SettingSQL.class);
    }

    public void save(FPlayer player) {
        useTransaction(sql -> {
            for (FPlayer.Setting setting : FPlayer.Setting.values()) {
                if (!player.isSetting(setting)) {
                    delete(player, setting);
                } else {
                    int updated = sql.updateSetting(player.getId(), setting.name(), player.getSettingValue(setting));
                    if (updated == 0) {
                        sql.insertSetting(player.getId(), setting.name(), player.getSettingValue(setting));
                    }
                }
            }
        });
    }

    public void load(FPlayer player) {
        player.getSettings().clear();

        useHandle(sql -> {
            for (FPlayer.Setting setting : FPlayer.Setting.values()) {
                sql.getSetting(player.getId(), setting.name())
                        .ifPresent(value -> player.setSetting(setting, value));
            }
        });
    }

    public Optional<String> get(FPlayer player, FPlayer.Setting setting) {
        return withHandle(sql -> sql.getSetting(player.getId(), setting.name()));
    }

    public void delete(FPlayer player, FPlayer.Setting setting) {
        useHandle(sql -> sql.deleteSetting(player.getId(), setting.name()));
    }

    public void insert(FPlayer player, FPlayer.Setting setting) {
        useHandle(sql -> sql.insertSetting(
                player.getId(),
                setting.name(),
                player.getSettingValue(setting))
        );
    }

    public void update(FPlayer player, FPlayer.Setting setting) {
        useHandle(sql -> sql.updateSetting(
                player.getId(),
                setting.name(),
                player.getSettingValue(setting))
        );
    }

    public void insertOrUpdate(FPlayer player, FPlayer.Setting setting) {
        useHandle(sql -> {
            int updated = sql.updateSetting(
                    player.getId(),
                    setting.name(),
                    player.getSettingValue(setting)
            );
            if (updated == 0) {
                sql.insertSetting(
                        player.getId(),
                        setting.name(),
                        player.getSettingValue(setting)
                );
            }
        });
    }
}