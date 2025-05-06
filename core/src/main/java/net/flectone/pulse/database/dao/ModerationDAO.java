package net.flectone.pulse.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.util.logging.FLogger;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ModerationDAO {

    private final Database database;
    private final FLogger fLogger;

    @Inject
    public ModerationDAO(Database database,
                         FLogger fLogger) {
        this.database = database;
        this.fLogger = fLogger;
    }

    public List<String> getPlayersNames(Moderation.Type moderationType) {
        List<String> names = new ArrayList<>();

        try (Connection connection = database.getConnection()) {
            String SQL_GET_BY_TYPE = "SELECT * FROM `moderation` JOIN `player` ON `player`.`id` = `moderation`.`player` WHERE `type` = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_BY_TYPE);
            preparedStatement.setInt(1, moderationType.ordinal());

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                names.add(resultSet.getString("name"));
            }

        } catch (SQLException e) {
            fLogger.warning(e);
        }

        return names;
    }

    public List<Moderation> get(FPlayer fPlayer, Moderation.Type moderationType) {
        List<Moderation> moderations = new ArrayList<>();
        if (fPlayer.isUnknown()) return moderations;

        try (Connection connection = database.getConnection()) {
            String SQL_GET_BY_PLAYER_AND_TYPE = "SELECT * FROM `moderation` WHERE `player` = ? AND `type` = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_BY_PLAYER_AND_TYPE);
            preparedStatement.setInt(1, fPlayer.getId());
            preparedStatement.setInt(2, moderationType.ordinal());

            ResultSet resultSet = preparedStatement.executeQuery();

            return addAll(moderations, resultSet, moderationType);
        } catch (SQLException e) {
            fLogger.warning(e);
        }

        return moderations;
    }

    public List<Moderation> getValid(FPlayer fPlayer, Moderation.Type moderationType) {
        List<Moderation> moderations = new ArrayList<>();
        if (fPlayer.isUnknown()) return moderations;

        try (Connection connection = database.getConnection()) {
            String SQL_GET_VALID_BY_PLAYER_AND_TYPE = "SELECT * FROM `moderation` WHERE `player` = ? AND `type` = ? AND `valid` = '1' AND (`time` = '-1' OR `time` > ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_VALID_BY_PLAYER_AND_TYPE);
            preparedStatement.setInt(1, fPlayer.getId());
            preparedStatement.setInt(2, moderationType.ordinal());
            preparedStatement.setLong(3, System.currentTimeMillis());

            ResultSet resultSet = preparedStatement.executeQuery();

            return addAll(moderations, resultSet, moderationType);
        } catch (SQLException e) {
            fLogger.warning(e);
        }

        return moderations;
    }

    public List<Moderation> getValid(Moderation.Type moderationType) {
        List<Moderation> moderations = new ArrayList<>();

        try (Connection connection = database.getConnection()) {
            String SQL_GET_VALID_BY_TYPE = "SELECT * FROM `moderation` WHERE `type` = ? AND `valid` = '1' AND (`time` = '-1' OR `time` > ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_VALID_BY_TYPE);
            preparedStatement.setInt(1, moderationType.ordinal());
            preparedStatement.setLong(2, System.currentTimeMillis());

            ResultSet resultSet = preparedStatement.executeQuery();

            return addAll(moderations, resultSet, moderationType);
        } catch (SQLException e) {
            fLogger.warning(e);
        }

        return moderations;
    }

    public List<String> getValidPlayersNames(Moderation.Type moderationType) {
        List<String> names = new ArrayList<>();

        try (Connection connection = database.getConnection()) {
            String SQL_GET_VALID_PLAYER_BY_TYPE = "SELECT * FROM `moderation` JOIN `player` ON `player`.`id` = `moderation`.`player` WHERE `type` = ? AND `valid` = '1' AND (`time` = '-1' OR `time` > ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_VALID_PLAYER_BY_TYPE);
            preparedStatement.setInt(1, moderationType.ordinal());
            preparedStatement.setLong(2, System.currentTimeMillis());

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                names.add(resultSet.getString("name"));
            }

        } catch (SQLException e) {
            fLogger.warning(e);
        }

        return names;
    }

    @Nullable
    public Moderation insert(FPlayer fTarget, long time, String reason, int moderatorID, Moderation.Type moderationType) {
        if (fTarget.isUnknown()) return null;

        try (Connection connection = database.getConnection()) {
            String SQL_INSERT = "INSERT INTO `moderation` (`player`, `date`, `time`, `reason`, `moderator`, `type`, `valid`) VALUES (?,?,?,?,?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
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
        } catch (SQLException e) {
            fLogger.warning(e);
        }

        return null;
    }

    public void updateValid(Moderation moderation) {
        try (Connection connection = database.getConnection()) {
            String SQL_UPDATE_VALID = "UPDATE `moderation` SET `valid` = ? WHERE `id` = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_UPDATE_VALID);
            preparedStatement.setBoolean(1, false);
            preparedStatement.setInt(2, moderation.getId());
            preparedStatement.execute();
        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }

    private List<Moderation> addAll(List<Moderation> list, ResultSet resultSet, Moderation.Type moderationType) throws SQLException {
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
}
