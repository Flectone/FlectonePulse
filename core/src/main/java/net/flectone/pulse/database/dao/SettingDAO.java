package net.flectone.pulse.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.database.sql.SettingSQL;
import net.flectone.pulse.model.FPlayer;
import org.jdbi.v3.core.Handle;

import java.util.Map;
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


    public void MIGRATION_0_6_0() {
        useHandleTransaction(handle -> {
            handle.createQuery("SELECT * FROM `player`")
                    .mapToMap()
                    .forEach(row -> {
                        int id = (Integer) row.get("id");

                        insertOldSetting(handle, id, row, "chat", FPlayer.Setting.CHAT);
                        insertOldSetting(handle, id, row, "locale", FPlayer.Setting.LOCALE);
                        insertOldSetting(handle, id, row, "world_prefix", FPlayer.Setting.WORLD_PREFIX);
                        insertOldSetting(handle, id, row, "stream_prefix", FPlayer.Setting.STREAM_PREFIX);
                        insertOldSetting(handle, id, row, "afk_suffix", FPlayer.Setting.AFK_SUFFIX);

                        String setting = (String) row.get("setting");
                        if (setting == null) return;

                        setting = setting.substring(1, setting.length() - 1);
                        String[] stringValues = setting.split(", ");
                        boolean[] settings = new boolean[stringValues.length];

                        for (int i = 0; i < stringValues.length; i++) {
                            settings[i] = Boolean.parseBoolean(stringValues[i]);
                        }

                        insertOldSetting(handle, id, settings, 0, FPlayer.Setting.STREAM);
                        insertOldSetting(handle, id, settings, 1, FPlayer.Setting.SPY);
                        insertOldSetting(handle, id, settings, 2, FPlayer.Setting.ADVANCEMENT);
                        insertOldSetting(handle, id, settings, 3, FPlayer.Setting.DEATH);
                        insertOldSetting(handle, id, settings, 4, FPlayer.Setting.JOIN);
                        insertOldSetting(handle, id, settings, 5, FPlayer.Setting.QUIT);
                        insertOldSetting(handle, id, settings, 6, FPlayer.Setting.AUTO);
                        insertOldSetting(handle, id, settings, 7, FPlayer.Setting.ME);
                        insertOldSetting(handle, id, settings, 8, FPlayer.Setting.TRY);
                        insertOldSetting(handle, id, settings, 9, FPlayer.Setting.DICE);
                        insertOldSetting(handle, id, settings, 10, FPlayer.Setting.BALL);
                        insertOldSetting(handle, id, settings, 11, FPlayer.Setting.MUTE);
                        insertOldSetting(handle, id, settings, 12, FPlayer.Setting.BAN);
                        insertOldSetting(handle, id, settings, 13, FPlayer.Setting.WARN);
                        insertOldSetting(handle, id, settings, 14, FPlayer.Setting.TELL);
                        insertOldSetting(handle, id, settings, 15, FPlayer.Setting.REPLY);
                        insertOldSetting(handle, id, settings, 16, FPlayer.Setting.MAIL);
                        insertOldSetting(handle, id, settings, 17, FPlayer.Setting.TICTACTOE);
                        insertOldSetting(handle, id, settings, 18, FPlayer.Setting.KICK);
                        insertOldSetting(handle, id, settings, 19, FPlayer.Setting.TRANSLATETO);
                        insertOldSetting(handle, id, settings, 20, FPlayer.Setting.BROADCAST);
                        insertOldSetting(handle, id, settings, 21, FPlayer.Setting.DO);
                        insertOldSetting(handle, id, settings, 22, FPlayer.Setting.COIN);
                        insertOldSetting(handle, id, settings, 23, FPlayer.Setting.AFK);
                        insertOldSetting(handle, id, settings, 24, FPlayer.Setting.POLL);
                        // SPIT index is 25 but this removed
                        insertOldSetting(handle, id, settings, 26, FPlayer.Setting.GREETING);
                        insertOldSetting(handle, id, settings, 27, FPlayer.Setting.ROCKPAPERSCISSORS);
                        insertOldSetting(handle, id, settings, 28, FPlayer.Setting.DISCORD);
                        insertOldSetting(handle, id, settings, 29, FPlayer.Setting.TELEGRAM);
                        insertOldSetting(handle, id, settings, 30, FPlayer.Setting.TWITCH);
                    });
        });
    }

    private void insertOldSetting(Handle handle, int id, Map<String, Object> row, String column, FPlayer.Setting setting) {
        String value = (String) row.get(column);
        if (value == null) return;

        insert(handle, id, setting, value);
    }

    private void insertOldSetting(Handle handle, int id, boolean[] settings, int index, FPlayer.Setting setting) {
        if (settings.length <= index) return;
        if (!settings[index]) return;

        insert(handle, id, setting, "");
    }

    private void insert(Handle handle, int playerId, FPlayer.Setting setting, String value) {
        handle.createUpdate("INSERT INTO `setting`(`player`, `type`, `value`) VALUES (:player, :type, :value)")
                .bind("player", playerId)
                .bind("type", setting.name())
                .bind("value", value)
                .execute();
    }
}