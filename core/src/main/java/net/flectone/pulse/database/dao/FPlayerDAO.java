package net.flectone.pulse.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.database.SQLType;
import net.flectone.pulse.file.Config;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Singleton
public class FPlayerDAO {

    private final String SQL_GET_ONLINE_PLAYERS = "SELECT * FROM `player` WHERE `online` = 1";
    private final String SQL_GET_PLAYERS = "SELECT * FROM `player`";
    private final String SQL_GET_PLAYER_WITH_IP = "SELECT DISTINCT * FROM `player` WHERE `ip` = ?";
    private final String SQL_GET_PLAYER_WITH_NAME = "SELECT DISTINCT * FROM `player` WHERE UPPER(`name`) LIKE UPPER(?)";
    private final String SQL_GET_PLAYER_WITH_UUID = "SELECT DISTINCT * FROM `player` WHERE `uuid` = ?";
    private final String SQL_GET_PLAYER_WITH_ID = "SELECT DISTINCT * FROM `player` WHERE `id` = ?";
    private final String SQL_UPDATE_PLAYER_WITH_ID = "UPDATE `player` SET `online` = ?, `uuid` = ?, `name` = ?, `ip` = ?, `chat` = ?, `locale` = ?, `world_prefix` = ?, `stream_prefix` = ?, `afk_suffix` = ?, `setting` = ? WHERE `id` = ?";
    private final String SQL_INSERT_PLAYER = "INSERT INTO `player` (`uuid`, `name`) VALUES (?, ?)";

    private final String SQLITE_INSERT_OR_IGNORE_FPLAYER = "INSERT OR IGNORE INTO `player` (`id`, `uuid`, `name`) VALUES (?, ?, ?)";
    private final String SQL_INSERT_OR_IGNORE_FPLAYER = "INSERT IGNORE INTO `player` (`id`, `uuid`, `name`) VALUES (?, ?, ?)";

    private final Config.Database config;
    private final Database database;
    private final FLogger fLogger;

    @Inject
    public FPlayerDAO(FileManager fileManager,
                      Database database,
                      FLogger fLogger) {
        this.config = fileManager.getConfig().getDatabase();
        this.database = database;
        this.fLogger = fLogger;
    }

    public void insertPlayer(UUID uuid, String name) {
        try (Connection connection = database.getConnection()) {

            // check name in database
            PreparedStatement statement = connection.prepareStatement(SQL_GET_PLAYER_WITH_NAME);
            statement.setString(1, name);

            ResultSet resultSet = statement.executeQuery();

            // if exists - return
            if (resultSet.next()) {
                UUID playerDatabaseUUID = UUID.fromString(resultSet.getString("uuid"));
                if (!uuid.equals(playerDatabaseUUID)) {
                    updatePlayer(resultSet.getInt("id"), uuid, name);
                }

                return;
            }

            // check uuid in database
            statement = connection.prepareStatement(SQL_GET_PLAYER_WITH_UUID);
            statement.setString(1, uuid.toString());

            resultSet = statement.executeQuery();

            // if exists - return
            if (resultSet.next()) {
                String playerDatabaseNAME = resultSet.getString("name");
                if (!name.equalsIgnoreCase(playerDatabaseNAME)) {
                    updatePlayer(resultSet.getInt("id"), uuid, name);
                }

                return;
            }

            statement = connection.prepareStatement(SQL_INSERT_PLAYER, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, uuid.toString());
            statement.setString(2, name);
            statement.executeUpdate();
        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }

    public void insertFPlayer(FPlayer fPlayer) {
        try (Connection connection = database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(config.getType() == Config.Database.Type.MYSQL ? SQL_INSERT_OR_IGNORE_FPLAYER : SQLITE_INSERT_OR_IGNORE_FPLAYER);
            statement.setInt(1, fPlayer.getId());
            statement.setString(2, fPlayer.getUuid().toString());
            statement.setString(3, fPlayer.getName());
            statement.executeUpdate();
        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }

    public void updateFPlayer(FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return;

        try (Connection connection = database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_PLAYER_WITH_ID);
            statement.setInt(1, fPlayer.isOnline() ? 1 : 0);
            statement.setString(2, fPlayer.getUuid().toString());
            statement.setString(3, fPlayer.getCurrentName());
            statement.setString(4, fPlayer.getIp());
            statement.setString(5, fPlayer.getChat());
            statement.setString(6, fPlayer.getLocale());
            statement.setString(7, fPlayer.getWorldPrefix());
            statement.setString(8, fPlayer.getStreamPrefix());
            statement.setString(9, fPlayer.getAfkSuffix());
            statement.setString(10, Arrays.toString(fPlayer.getSettings()));
            statement.setInt(11, fPlayer.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }

    @NotNull
    public List<FPlayer> getOnlineFPlayers() {
        return getFPlayers(SQL_GET_ONLINE_PLAYERS);
    }

    @NotNull
    public List<FPlayer> getFPlayers() {
        return getFPlayers(SQL_GET_PLAYERS);
    }

    @NotNull
    public FPlayer getFPlayer(String name) {
        return getFPlayer(SQL_GET_PLAYER_WITH_NAME, name, SQLType.STRING);
    }

    @NotNull
    public FPlayer getFPlayer(InetAddress inetAddress) {
        return getFPlayer(SQL_GET_PLAYER_WITH_IP, inetAddress.getHostName(), SQLType.STRING);
    }

    @NotNull
    public FPlayer getFPlayer(UUID uuid) {
        return getFPlayer(SQL_GET_PLAYER_WITH_UUID, uuid.toString(), SQLType.STRING);
    }

    @NotNull
    public FPlayer getFPlayer(int id) {
        return getFPlayer(SQL_GET_PLAYER_WITH_ID, id, SQLType.INTEGER);
    }

    private void updatePlayer(int playerID, UUID uuid, String name) {
        fLogger.warning("Found player " + name + " with different UUID or name, will now use UUID: " + uuid.toString() + " and name: " + name);

        try (Connection connection = database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_PLAYER_WITH_ID);
            statement.setInt(1, 1);
            statement.setString(2, uuid.toString());
            statement.setString(3, name);
            statement.setString(4, null);
            statement.setString(5, null);
            statement.setString(6, null);
            statement.setString(7, null);
            statement.setString(8, null);
            statement.setString(9, null);
            statement.setString(10, null);
            statement.setInt(11, playerID);
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
                return getFPlayerFromResultSet(resultSet);
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
                fPlayers.add(getFPlayerFromResultSet(resultSet));
            }

        } catch (SQLException e) {
            fLogger.warning(e);
        }

        return fPlayers;
    }

    @NotNull
    private FPlayer getFPlayerFromResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        boolean isOnline = resultSet.getInt("online") == 1;
        UUID uuid = UUID.fromString(resultSet.getString("uuid"));
        String name = resultSet.getString("name");
        String ip = resultSet.getString("ip");
        String chat = resultSet.getString("chat");
        String locale = resultSet.getString("locale");
        String worldPrefix = resultSet.getString("world_prefix");
        String streamPrefix = resultSet.getString("stream_prefix");
        String afkSuffix = resultSet.getString("afk_suffix");
        boolean[] settings = FPlayer.toBooleanArray(resultSet.getString("setting"));

        FPlayer fPlayer = new FPlayer(id, name, uuid);
        fPlayer.setOnline(isOnline);
        fPlayer.setIp(ip);
        fPlayer.setChat(chat);
        fPlayer.setLocale(locale);
        fPlayer.setWorldPrefix(worldPrefix);
        fPlayer.setStreamPrefix(streamPrefix);
        fPlayer.setAfkSuffix(afkSuffix);

        if (settings != null) {
            System.arraycopy(settings, 0, fPlayer.getSettings(), 0, settings.length);
        }

        return fPlayer;
    }
}
