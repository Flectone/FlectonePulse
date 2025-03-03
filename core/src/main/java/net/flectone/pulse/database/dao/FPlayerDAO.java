package net.flectone.pulse.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.database.SQLType;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.logging.FLogger;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
public class FPlayerDAO {

    private final Config.Database config;
    private final Database database;
    private final FLogger fLogger;

    @Inject private SettingDAO settingDAO;

    @Inject
    public FPlayerDAO(FileManager fileManager,
                      Database database,
                      FLogger fLogger) {
        this.config = fileManager.getConfig().getDatabase();
        this.database = database;
        this.fLogger = fLogger;
    }

    public boolean insert(UUID uuid, String name) {
        try (Connection connection = database.getConnection()) {

            // check name in database
            String SQL_GET_BY_NAME = "SELECT DISTINCT * FROM `player` WHERE UPPER(`name`) LIKE UPPER(?)";
            PreparedStatement statement = connection.prepareStatement(SQL_GET_BY_NAME);
            statement.setString(1, name);

            ResultSet resultSet = statement.executeQuery();

            // if exists - return
            if (resultSet.next()) {
                UUID playerDatabaseUUID = UUID.fromString(resultSet.getString("uuid"));
                if (!uuid.equals(playerDatabaseUUID)) {
                    updateAndWarn(resultSet.getInt("id"), uuid, name);
                }

                return false;
            }

            // check uuid in database
            String SQL_GET_BY_UUID = "SELECT DISTINCT * FROM `player` WHERE `uuid` = ?";
            statement = connection.prepareStatement(SQL_GET_BY_UUID);
            statement.setString(1, uuid.toString());

            resultSet = statement.executeQuery();

            // if exists - return
            if (resultSet.next()) {
                String playerDatabaseNAME = resultSet.getString("name");
                if (!name.equalsIgnoreCase(playerDatabaseNAME)) {
                    updateAndWarn(resultSet.getInt("id"), uuid, name);
                }

                return false;
            }

            String SQL_INSERT = "INSERT INTO `player` (`uuid`, `name`) VALUES (?, ?)";
            statement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, uuid.toString());
            statement.setString(2, name);
            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            fLogger.warning(e);
        }

        return false;
    }

    public void insertOrIgnore(FPlayer fPlayer) {
        try (Connection connection = database.getConnection()) {
            String SQLITE_INSERT_OR_IGNORE = "INSERT OR IGNORE INTO `player` (`id`, `uuid`, `name`) VALUES (?, ?, ?)";
            String MYSQL_INSERT_OR_IGNORE = "INSERT IGNORE INTO `player` (`id`, `uuid`, `name`) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(config.getType() == Config.Database.Type.MYSQL ? MYSQL_INSERT_OR_IGNORE : SQLITE_INSERT_OR_IGNORE);
            statement.setInt(1, fPlayer.getId());
            statement.setString(2, fPlayer.getUuid().toString());
            statement.setString(3, fPlayer.getName());
            statement.executeUpdate();
        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }

    public void save(FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return;

        update(fPlayer.getId(), fPlayer.isOnline(), fPlayer.getUuid(), fPlayer.getCurrentName());
    }

    @NotNull
    public List<FPlayer> getOnlineFPlayers() {
        return getFPlayers("SELECT * FROM `player` WHERE `online` = 1");
    }

    @NotNull
    public List<FPlayer> getFPlayers() {
        return getFPlayers("SELECT * FROM `player`");
    }

    @NotNull
    public FPlayer getFPlayer(String name) {
        return getFPlayer("SELECT DISTINCT * FROM `player` WHERE UPPER(`name`) LIKE UPPER(?)", name, SQLType.STRING);
    }

    @NotNull
    public FPlayer getFPlayer(InetAddress inetAddress) {
        return getFPlayer("SELECT DISTINCT * FROM `player` WHERE `ip` = ?", inetAddress.getHostName(), SQLType.STRING);
    }

    @NotNull
    public FPlayer getFPlayer(UUID uuid) {
        return getFPlayer("SELECT DISTINCT * FROM `player` WHERE `uuid` = ?", uuid.toString(), SQLType.STRING);
    }

    @NotNull
    public FPlayer getFPlayer(int id) {
        return getFPlayer("SELECT DISTINCT * FROM `player` WHERE `id` = ?", id, SQLType.INTEGER);
    }

    private void updateAndWarn(int id, UUID uuid, String name) {
        fLogger.warning("Found player " + name + " with different UUID or name, will now use UUID: " + uuid.toString() + " and name: " + name);
        update(id, true, uuid, name);
    }

    private void update(int id, boolean online, UUID uuid, String name) {
        try (Connection connection = database.getConnection()) {
            String SQL_UPDATE_BY_ID = "UPDATE `player` SET `online` = ?, `uuid` = ?, `name` = ? WHERE `id` = ?";
            PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_ID);
            statement.setInt(1, online ? 1 : 0);
            statement.setString(2, uuid.toString());
            statement.setString(3, name);
            statement.setInt(4, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }

    @NotNull
    private FPlayer getFPlayer(String SQL, Object object, SQLType sqlType) {
        try (Connection connection = database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL);

            switch (sqlType) {
                case STRING -> statement.setString(1, (String) object);
                case INTEGER -> statement.setInt(1, (int) object);
            }

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getFPlayerFromResultSet(resultSet, true);
            }

        } catch (SQLException e) {
            fLogger.warning(e);
        }

        return FPlayer.UNKNOWN;
    }

    @NotNull
    private List<FPlayer> getFPlayers(String SQL) {
        List<FPlayer> fPlayers = new ArrayList<>();

        try (Connection connection = database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                fPlayers.add(getFPlayerFromResultSet(resultSet, false));
            }

        } catch (SQLException e) {
            fLogger.warning(e);
        }

        return fPlayers;
    }

    @NotNull
    private FPlayer getFPlayerFromResultSet(ResultSet resultSet, boolean loadSetting) throws SQLException {
        int id = resultSet.getInt("id");
        boolean isOnline = resultSet.getInt("online") == 1;
        UUID uuid = UUID.fromString(resultSet.getString("uuid"));
        String name = resultSet.getString("name");

        FPlayer fPlayer = new FPlayer(id, name, uuid);
        fPlayer.setOnline(isOnline);

        if (loadSetting) {
            settingDAO.load(fPlayer);
        }

        return fPlayer;
    }
}
