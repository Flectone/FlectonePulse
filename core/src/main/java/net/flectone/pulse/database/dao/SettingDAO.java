package net.flectone.pulse.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.logging.FLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Singleton
public class SettingDAO {

    private final Database database;
    private final FLogger fLogger;

    @Inject
    public SettingDAO(Database database,
                      FLogger fLogger) {
        this.database = database;
        this.fLogger = fLogger;
    }

    public void save(FPlayer fPlayer) {
        for (FPlayer.Setting setting : FPlayer.Setting.values()) {
            if (!fPlayer.isSetting(setting)) {
                delete(fPlayer, setting);
                continue;
            }

            insertOrUpdate(fPlayer, setting);
        }
    }

    public void load(FPlayer fPlayer) {
        for (FPlayer.Setting setting : FPlayer.Setting.values()) {
            Optional<String> optionalValue = get(fPlayer, setting);
            if (optionalValue.isEmpty()) continue;

            fPlayer.setSetting(setting, optionalValue.get());
        }
    }

    public Optional<String> get(FPlayer fPlayer, FPlayer.Setting setting) {
        try (Connection connection = database.getConnection()) {
            String SQL_GET = "SELECT * FROM `setting` WHERE `player` = ? AND `type` = ?";
            PreparedStatement statement = connection.prepareStatement(SQL_GET);
            statement.setInt(1, fPlayer.getId());
            statement.setString(2, setting.name());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.ofNullable(resultSet.getString("value"));
            }

        } catch (SQLException e) {
            fLogger.warning(e);
        }

        return Optional.empty();
    }

    public void delete(FPlayer fPlayer, FPlayer.Setting setting) {
        try (Connection connection = database.getConnection()) {
            String SQL_DELETE = "DELETE FROM `setting` WHERE `player` = ? AND `type` = ?";
            PreparedStatement statement = connection.prepareStatement(SQL_DELETE);
            statement.setInt(1, fPlayer.getId());
            statement.setString(2, setting.name());
            statement.executeUpdate();

        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }

    public void insert(FPlayer fPlayer, FPlayer.Setting setting) {
        try (Connection connection = database.getConnection()) {
            insert(connection, fPlayer.getId(), setting, fPlayer.getSettingValue(setting));

        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }

    public void update(FPlayer fPlayer, FPlayer.Setting setting) {
        try (Connection connection = database.getConnection()) {
            String SQL_UPDATE = "UPDATE `setting` SET `value` =  ? WHERE `player` = ? AND `type` = ?";
            PreparedStatement statement = connection.prepareStatement(SQL_UPDATE);
            statement.setString(1, fPlayer.getSettingValue(setting));
            statement.setInt(2, fPlayer.getId());
            statement.setString(3, setting.name());
            statement.executeUpdate();

        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }

    public void insertOrUpdate(FPlayer fPlayer, FPlayer.Setting setting) {
        Optional<String> optionalValue = get(fPlayer, setting);
        if (optionalValue.isPresent()) {
            update(fPlayer, setting);
        } else {
            insert(fPlayer, setting);
        }
    }

    public void MIGRATION_0_6_0() {
        try (Connection connection = database.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `player`");

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");

                insertOldSetting(connection, id, resultSet, "chat", FPlayer.Setting.CHAT);
                insertOldSetting(connection, id, resultSet, "locale", FPlayer.Setting.LOCALE);
                insertOldSetting(connection, id, resultSet, "world_prefix", FPlayer.Setting.WORLD_PREFIX);
                insertOldSetting(connection, id, resultSet, "stream_prefix", FPlayer.Setting.STREAM_PREFIX);
                insertOldSetting(connection, id, resultSet, "afk_suffix", FPlayer.Setting.AFK_SUFFIX);

                String setting = resultSet.getString("setting");
                if (setting == null) continue;

                setting = setting.substring(1, setting.length() - 1);

                String[] stringValues = setting.split(", ");

                boolean[] settings = new boolean[stringValues.length];
                for (int i = 0; i < stringValues.length; i++) {
                    settings[i] = Boolean.parseBoolean(stringValues[i]);
                }

                insertOldSetting(connection, id, settings, 0, FPlayer.Setting.STREAM);
                insertOldSetting(connection, id, settings, 1, FPlayer.Setting.SPY);
                insertOldSetting(connection, id, settings, 2, FPlayer.Setting.ADVANCEMENT);
                insertOldSetting(connection, id, settings, 3, FPlayer.Setting.DEATH);
                insertOldSetting(connection, id, settings, 4, FPlayer.Setting.JOIN);
                insertOldSetting(connection, id, settings, 5, FPlayer.Setting.QUIT);
                insertOldSetting(connection, id, settings, 6, FPlayer.Setting.AUTO);
                insertOldSetting(connection, id, settings, 7, FPlayer.Setting.ME);
                insertOldSetting(connection, id, settings, 8, FPlayer.Setting.TRY);
                insertOldSetting(connection, id, settings, 9, FPlayer.Setting.DICE);
                insertOldSetting(connection, id, settings, 10, FPlayer.Setting.BALL);
                insertOldSetting(connection, id, settings, 11, FPlayer.Setting.MUTE);
                insertOldSetting(connection, id, settings, 12, FPlayer.Setting.BAN);
                insertOldSetting(connection, id, settings, 13, FPlayer.Setting.WARN);
                insertOldSetting(connection, id, settings, 14, FPlayer.Setting.TELL);
                insertOldSetting(connection, id, settings, 15, FPlayer.Setting.REPLY);
                insertOldSetting(connection, id, settings, 16, FPlayer.Setting.MAIL);
                insertOldSetting(connection, id, settings, 17, FPlayer.Setting.TICTACTOE);
                insertOldSetting(connection, id, settings, 18, FPlayer.Setting.KICK);
                insertOldSetting(connection, id, settings, 19, FPlayer.Setting.TRANSLATETO);
                insertOldSetting(connection, id, settings, 20, FPlayer.Setting.BROADCAST);
                insertOldSetting(connection, id, settings, 21, FPlayer.Setting.DO);
                insertOldSetting(connection, id, settings, 22, FPlayer.Setting.COIN);
                insertOldSetting(connection, id, settings, 23, FPlayer.Setting.AFK);
                insertOldSetting(connection, id, settings, 24, FPlayer.Setting.POLL);
                // SPIT index is 25 but this removed
                insertOldSetting(connection, id, settings, 26, FPlayer.Setting.GREETING);
                insertOldSetting(connection, id, settings, 27, FPlayer.Setting.ROCKPAPERSCISSORS);
                insertOldSetting(connection, id, settings, 28, FPlayer.Setting.DISCORD);
                insertOldSetting(connection, id, settings, 29, FPlayer.Setting.TELEGRAM);
                insertOldSetting(connection, id, settings, 30, FPlayer.Setting.TWITCH);
            }

        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }

    private void insert(Connection connection, int id, FPlayer.Setting setting, String value) throws SQLException {
        String SQL_INSERT = "INSERT INTO `setting`(`player`, `type`, `value`) VALUES (?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        statement.setInt(1, id);
        statement.setString(2, setting.name());
        statement.setString(3, value);
        statement.executeUpdate();
    }

    private void insertOldSetting(Connection connection, int id, ResultSet resultSet, String column, FPlayer.Setting setting) throws SQLException {
        String value = resultSet.getString(column);
        if (value == null) return;

        insert(connection, id, setting, value);
    }

    private void insertOldSetting(Connection connection, int id, boolean[] settings, int index, FPlayer.Setting setting) throws SQLException {
        if (settings.length <= index) return;
        if (!settings[index]) return;

        insert(connection, id, setting, "");
    }
}
