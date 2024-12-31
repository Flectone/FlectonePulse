package net.flectone.pulse.database;

import com.google.inject.Inject;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.mail.model.Mail;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.sql.*;
import java.util.*;

public abstract class Database {

    protected String SQL_DELETE_IGNORE = "DELETE FROM `ignore` WHERE `id` = ?";
    protected String SQL_DELETE_MAIL = "DELETE FROM `mail` WHERE `id` = ?";
    protected String SQL_DELETE_PLAYER_COLOR_WITH_NUMBER_AND_SETTING = "DELETE FROM `player_color` WHERE `number` = ? AND `player` = ?";
    protected String SQL_DELETE_PLAYER_COLOR_WITH_ID = "DELETE FROM `player_color` WHERE `player` = ?";

    protected String SQL_GET_ONLINE_PLAYERS = "SELECT * FROM `player` WHERE `online` = 1";
    protected String SQL_GET_PLAYERS = "SELECT * FROM `player`";
    protected String SQL_GET_PLAYER_WITH_IP = "SELECT DISTINCT * FROM `player` WHERE `ip` = ?";
    protected String SQL_GET_COLORS_WITH_NAME = "SELECT * FROM `color` WHERE `name` = ?";
    protected String SQL_GET_MODERATIONS_WITH_TYPE = "SELECT * FROM `moderation` JOIN `player` ON `player`.`id` = `moderation`.`player` WHERE `type` = ?";
    protected String SQL_GET_VALID_MODERATIONS_WITH_TYPE = "SELECT * FROM `moderation` JOIN `player` ON `player`.`id` = `moderation`.`player` WHERE `type` = ? AND `valid` = '1' AND (`time` = '-1' OR `time` > ?)";
    protected String SQL_GET_VALID_MODERATIONS_WITH_PLAYER_AND_TYPE = "SELECT * FROM `moderation` WHERE `player` = ? AND `type` = ? AND `valid` = '1' AND (`time` = '-1' OR `time` > ?)";
    protected String SQL_GET_PLAYER_WITH_NAME = "SELECT DISTINCT * FROM `player` WHERE UPPER(`name`) LIKE UPPER(?)";
    protected String SQL_GET_MAILS_WITH_RECEIVER = "SELECT * FROM `mail` WHERE `receiver` = ?";
    protected String SQL_GET_MAILS_WITH_SENDER = "SELECT * FROM `mail` WHERE `sender` = ?";
    protected String SQL_GET_MODERATIONS_WITH_PLAYER_AND_TYPE = "SELECT * FROM `moderation` WHERE `player` = ? AND `type` = ?";
    protected String SQL_GET_PLAYER_WITH_UUID = "SELECT DISTINCT * FROM `player` WHERE `uuid` = ?";
    protected String SQL_GET_PLAYER_WITH_ID = "SELECT DISTINCT * FROM `player` WHERE `id` = ?";
    protected String SQL_GET_VALID_MODERATIONS_WITH_ID_AND_TYPE_AND_TIME =  "SELECT * FROM `moderation` WHERE `player` = ? AND `type` = ? AND `valid` = '1' AND (`time` = '-1' OR `time` > ?)";
    protected String SQL_GET_PLAYER_COLORS = "SELECT * FROM `player_color` LEFT JOIN `color` ON `player_color`.`color` = `color`.`id` WHERE `player_color`.`player` = ?";
    protected String SQL_GET_IGNORES_WITH_INITIATOR = "SELECT * FROM `ignore` WHERE `initiator` = ?";
    protected String SQL_GET_IGNORE_WITH_INITIATOR_AND_TARGET = "SELECT * FROM `ignore` WHERE `initiator` = ? AND `target` = ?";

    protected String SQL_UPDATE_PLAYER_WITH_ID = "UPDATE `player` SET " +
            "`online` = ?, `uuid` = ?, `name` = ?, `ip` = ?, `chat` = ?, " +
            "`locale` = ?, `world_prefix` = ?, `stream_prefix` = ?, " +
            "`afk_suffix` = ?, `setting` = ? WHERE `id` = ?";
    protected String SQL_UPDATE_PLAYER_ONLINE = "UPDATE player SET online = ?";

    protected String SQL_INSERT_OR_IGNORE_FPLAYER = "INSERT IGNORE INTO `player` (`id`, `uuid`, `name`) VALUES (?, ?, ?)";

    protected String SQL_INSERT_PLAYER = "INSERT INTO `player` (`uuid`, `name`) VALUES (?, ?)";
    protected String SQL_INSERT_MODERATION = "INSERT INTO `moderation` (`player`, `date`, `time`, `reason`, `moderator`, `type`, `valid`) VALUES (?,?,?,?,?,?,?)";
    protected String SQL_INSERT_IGNORE = "INSERT INTO `ignore` (`date`, `initiator`, `target`) VALUES (?,?,?)";
    protected String SQL_INSERT_MAIL = "INSERT INTO `mail` (`date`, `sender`, `receiver`, `message`) VALUES (?,?,?,?)";

    protected String SQL_UPDATE_VALID_MODERATION = "UPDATE `moderation` SET `valid` = ? WHERE `id` = ?";

    protected String SQL_INSERT_OR_UPDATE_PLAYER_COLOR = "INSERT INTO `player_color` (`number`, `player`, `color`) " +
            "VALUES (?,?,?) " +
            "ON DUPLICATE KEY UPDATE " +
            "`number` = VALUES(`number`), `player` = VALUES(`player`), `color` = VALUES(`color`)";
    protected String SQL_INSERT_OR_UPDATE_COLOR = "INSERT INTO `color` (`name`) " +
            "VALUES (?) " +
            "ON DUPLICATE KEY UPDATE " +
            "`name` = VALUES(`name`)";


    public abstract void connect() throws SQLException, IOException;
    public abstract void disconnect();

    public abstract Connection getConnection() throws SQLException;

    public abstract void init() throws SQLException;

    @Inject
    protected FLogger fLogger;

    @Inject
    private DatabaseThread databaseThread;

    public void execute(Runnable runnable) {
        databaseThread.execute(runnable);
    }

    public void updateColors(FPlayer fPlayer) throws SQLException {
        try (Connection connection = getConnection()) {
            Map<String, String> colors = fPlayer.getColors();
            if (colors == null) {
                PreparedStatement preparedStatement = connection.prepareStatement(SQL_DELETE_PLAYER_COLOR_WITH_ID);
                preparedStatement.setInt(1, fPlayer.getId());
                preparedStatement.executeUpdate();
                return;
            }

            if (colors.isEmpty()) {
                PreparedStatement preparedStatement = connection.prepareStatement(SQL_DELETE_PLAYER_COLOR_WITH_ID);
                preparedStatement.setInt(1, fPlayer.getId());
                preparedStatement.executeUpdate();
                return;
            }

            for (Map.Entry<String, String> entry : colors.entrySet()) {
                PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_COLORS_WITH_NAME);
                preparedStatement.setString(1, String.valueOf(entry.getValue()));

                ResultSet colorResult = preparedStatement.executeQuery();

                int color = -1;
                if (colorResult.next()) {
                    color = colorResult.getInt("id");
                } else {
                    preparedStatement = connection.prepareStatement(SQL_INSERT_OR_UPDATE_COLOR, Statement.RETURN_GENERATED_KEYS);
                    preparedStatement.setString(1, String.valueOf(entry.getValue()));
                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows > 0) {
                        colorResult = preparedStatement.getGeneratedKeys();
                        if (colorResult.next()) {
                            color = colorResult.getInt(1);
                        }
                    }
                }

                if (color == -1) {
                    continue;
                }

                preparedStatement = connection.prepareStatement(SQL_DELETE_PLAYER_COLOR_WITH_NUMBER_AND_SETTING);
                preparedStatement.setInt(1, Integer.parseInt(entry.getKey()));
                preparedStatement.setInt(2, fPlayer.getId());
                preparedStatement.executeUpdate();

                preparedStatement = connection.prepareStatement(SQL_INSERT_OR_UPDATE_PLAYER_COLOR);
                preparedStatement.setInt(1, Integer.parseInt(entry.getKey()));
                preparedStatement.setInt(2, fPlayer.getId());
                preparedStatement.setInt(3, color);
                preparedStatement.executeUpdate();
            }
        }
    }

    @Nullable
    public Moderation insertModeration(FPlayer fTarget, long time, String reason, int moderatorID, Moderation.Type moderationType) throws SQLException {
        if (fTarget.isUnknown()) return null;

        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT_MODERATION, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, fTarget.getId());
            long date = System.currentTimeMillis();
            preparedStatement.setLong(2, date);
            preparedStatement.setLong(3, time);
            preparedStatement.setString(4, reason);
            preparedStatement.setInt(5, moderatorID);
            preparedStatement.setInt(6, moderationType.ordinal());
            preparedStatement.setInt(7, 1);
            preparedStatement.executeUpdate();

            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                return new Moderation(
                        resultSet.getInt(1),
                        fTarget.getId(),
                        date,
                        time,
                        reason,
                        moderatorID,
                        moderationType,
                        true
                );
            }
        }

        return null;
    }

    public void setInvalidModeration(Moderation moderation) throws SQLException {
        moderation.setInvalid();

        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_UPDATE_VALID_MODERATION);
            preparedStatement.setBoolean(1, false);
            preparedStatement.setInt(2, moderation.getId());
            preparedStatement.execute();
        }
    }

    @Nullable
    public Ignore insertIgnore(FPlayer fSender, FPlayer fIgnored) throws SQLException {
        if (fSender.isUnknown() || fIgnored.isUnknown()) return null;

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_INSERT_IGNORE);
            statement.setLong(1, System.currentTimeMillis());
            statement.setInt(2, fSender.getId());
            statement.setInt(3, fIgnored.getId());
            statement.executeUpdate();


            statement = connection.prepareStatement(SQL_GET_IGNORE_WITH_INITIATOR_AND_TARGET);
            statement.setInt(1, fSender.getId());
            statement.setInt(2, fIgnored.getId());

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new Ignore(resultSet.getInt(1),
                        resultSet.getLong(2),
                        fIgnored.getId()
                );
            }
        }

        return null;
    }

    @Nullable
    public Mail insertMail(FPlayer fPlayer, FPlayer fReceiver, String message) throws SQLException {
        if (fPlayer.isUnknown() || fReceiver.isUnknown()) return null;

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_INSERT_MAIL, Statement.RETURN_GENERATED_KEYS);

            long date = System.currentTimeMillis();

            statement.setLong(1, System.currentTimeMillis());
            statement.setInt(2, fPlayer.getId());
            statement.setInt(3, fReceiver.getId());
            statement.setString(4, message);
            statement.executeUpdate();

            ResultSet resultSet = statement.getGeneratedKeys();

            if (resultSet.next()) {
                return new Mail(
                        resultSet.getInt(1),
                        date,
                        fPlayer.getId(),
                        fReceiver.getId(),
                        message
                );
            }
        }

        return null;
    }

    public void removeIgnore(Ignore ignore) throws SQLException {
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_DELETE_IGNORE);
            preparedStatement.setInt(1, ignore.id());
            preparedStatement.executeUpdate();
        }
    }

    public void removeMail(Mail mail) throws SQLException {
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_DELETE_MAIL);
            preparedStatement.setInt(1, mail.id());
            preparedStatement.executeUpdate();
        }
    }

    protected void executeFile(InputStream inputStream) throws SQLException, IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            StringBuilder builder = new StringBuilder();

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("--")) continue;

                builder.append(line);

                if (line.endsWith(";")) {
                    statement.execute(builder.toString());
                    builder.setLength(0);
                }
            }
        }
    }

    public void insertPlayer(UUID uuid, String name) throws SQLException {
        try (Connection connection = getConnection()) {

            // check name in database
            PreparedStatement statement = connection.prepareStatement(SQL_GET_PLAYER_WITH_NAME);
            statement.setString(1, name);

            ResultSet resultSet = statement.executeQuery();

            // if exists - return
            if (resultSet.next()) {
                UUID playerDatabaseUUID = UUID.fromString(resultSet.getString("uuid"));
                if (!uuid.equals(playerDatabaseUUID)) {
                    replacePlayer(resultSet.getInt("id"), uuid, name);
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
                    replacePlayer(resultSet.getInt("id"), uuid, name);
                }

                return;
            }

            statement = connection.prepareStatement(SQL_INSERT_PLAYER, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, uuid.toString());
            statement.setString(2, name);
            statement.executeUpdate();
        }
    }

    private void replacePlayer(int playerID, UUID uuid, String name) throws SQLException {
        fLogger.warning("Found player " + name + " with different UUID or name, will now use UUID: " + uuid.toString() + " and name: " + name);

        try (Connection connection = getConnection()) {
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
        }
    }

    public void insertFPlayer(FPlayer fPlayer) throws SQLException {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_INSERT_OR_IGNORE_FPLAYER);
            statement.setInt(1, fPlayer.getId());
            statement.setString(2, fPlayer.getUuid().toString());
            statement.setString(3, fPlayer.getName());
            statement.executeUpdate();
        }
    }

    public void updateFPlayer(FPlayer fPlayer) throws SQLException {
        if (fPlayer.isUnknown()) return;

        try (Connection connection = getConnection()) {
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
        }
    }

    public void setIgnores(FPlayer fPlayer) throws SQLException {
        if (fPlayer.isUnknown()) return;

        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_IGNORES_WITH_INITIATOR);
            preparedStatement.setInt(1, fPlayer.getId());

            ResultSet resultSet = preparedStatement.executeQuery();

            List<Ignore> ignores = fPlayer.getIgnores();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                long date = resultSet.getLong("date");
                int target = resultSet.getInt("target");

                ignores.add(new Ignore(id, date, target));
            }
        }
    }

    public List<Mail> getMails(FPlayer fPlayer) throws SQLException {
        List<Mail> mails = new ArrayList<>();

        if (fPlayer.isUnknown()) return mails;

        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_MAILS_WITH_RECEIVER);
            preparedStatement.setInt(1, fPlayer.getId());

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                long date = resultSet.getLong("date");
                int sender = resultSet.getInt("sender");
                String message = resultSet.getString("message");

                mails.add(new Mail(id, date, sender, fPlayer.getId(), message));
            }
        }

        return mails;
    }

    public List<String> getModerationsNames(Moderation.Type moderationType) throws SQLException {
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_MODERATIONS_WITH_TYPE);
            preparedStatement.setInt(1, moderationType.ordinal());

            List<String> names = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                names.add(resultSet.getString("name"));
            }

            return names;
        }
    }

    public List<Moderation> getModerations(FPlayer fPlayer, Moderation.Type moderationType) throws SQLException {
        if (fPlayer.isUnknown()) return new ArrayList<>();

        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_MODERATIONS_WITH_PLAYER_AND_TYPE);
            preparedStatement.setInt(1, fPlayer.getId());
            preparedStatement.setInt(2, moderationType.ordinal());

            return getModerationsFromStatement(preparedStatement, moderationType);
        }
    }

    public List<Moderation> getValidModerations(FPlayer fPlayer, Moderation.Type moderationType) throws SQLException {
        if (fPlayer.isUnknown()) return new ArrayList<>();

        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_VALID_MODERATIONS_WITH_PLAYER_AND_TYPE);
            preparedStatement.setInt(1, fPlayer.getId());
            preparedStatement.setInt(2, moderationType.ordinal());
            preparedStatement.setLong(3, System.currentTimeMillis());

            return getModerationsFromStatement(preparedStatement, moderationType);
        }
    }

    public List<Moderation> getValidModerations(Moderation.Type moderationType) throws SQLException {
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_VALID_MODERATIONS_WITH_TYPE);
            preparedStatement.setInt(1, moderationType.ordinal());
            preparedStatement.setLong(2, System.currentTimeMillis());

            return getModerationsFromStatement(preparedStatement, moderationType);
        }
    }

    public List<String> getValidModerationsNames(Moderation.Type moderationType) throws SQLException {
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_VALID_MODERATIONS_WITH_TYPE);
            preparedStatement.setInt(1, moderationType.ordinal());
            preparedStatement.setLong(2, System.currentTimeMillis());

            List<String> names = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                names.add(resultSet.getString("name"));
            }

            return names;
        }
    }

    public void setColors(FPlayer fPlayer) throws SQLException {
        if (fPlayer.isUnknown()) return;

        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_PLAYER_COLORS);
            preparedStatement.setInt(1, fPlayer.getId());

            ResultSet resultSet = preparedStatement.executeQuery();

            Map<String, String> colors = fPlayer.getColors();

            while (resultSet.next()) {

                String number = String.valueOf(resultSet.getInt("number"));

                String color = resultSet.getString("name");

                colors.put(number, color);
            }
        }
    }

    public List<FPlayer> getOnlineFPlayers() throws SQLException {
        List<FPlayer> fPlayers = new ArrayList<>();

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_GET_ONLINE_PLAYERS);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                fPlayers.add(getFPlayerFromResultSet(resultSet));
            }
        }

        return fPlayers;
    }

    public List<FPlayer> getFPlayers() throws SQLException {
        List<FPlayer> fPlayers = new ArrayList<>();

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_GET_PLAYERS);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                fPlayers.add(getFPlayerFromResultSet(resultSet));
            }
        }

        return fPlayers;
    }

    @NotNull
    public FPlayer getFPlayer(String name) throws SQLException {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_GET_PLAYER_WITH_NAME);
            statement.setString(1, name);

            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) return FPlayer.UNKNOWN;

            return getFPlayerFromResultSet(resultSet);
        }
    }

    @NotNull
    public FPlayer getFPlayer(InetAddress inetAddress) throws SQLException {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_GET_PLAYER_WITH_IP);
            statement.setString(1, inetAddress.getHostName());

            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) return FPlayer.UNKNOWN;

            return getFPlayerFromResultSet(resultSet);
        }
    }

    @NotNull
    public FPlayer getFPlayer(UUID uuid) throws SQLException {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_GET_PLAYER_WITH_UUID);
            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) return FPlayer.UNKNOWN;

            return getFPlayerFromResultSet(resultSet);
        }
    }

    @NotNull
    public FPlayer getFPlayer(int id) throws SQLException {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_GET_PLAYER_WITH_ID);
            statement.setInt(1, id);

            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) return FPlayer.UNKNOWN;

            return getFPlayerFromResultSet(resultSet);
        }
    }

    private FPlayer getFPlayerFromResultSet(ResultSet resultSet) throws SQLException{
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

    private List<Moderation> getModerationsFromStatement(PreparedStatement statement, Moderation.Type moderationType) throws SQLException {
        List<Moderation> list = new ArrayList<>();

        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            int player = resultSet.getInt("player");
            long date = resultSet.getLong("date");
            long time = resultSet.getLong("time");
            String reason = resultSet.getString("reason");
            int moderator = resultSet.getInt("moderator");
            boolean valid = resultSet.getBoolean("valid");

            list.add(new Moderation(id, player, date, time, reason, moderator, moderationType, valid));
        }

        return list;
    }

    public enum Type {
        SQLITE,
        MYSQL
    }
}
