package net.flectone.pulse.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Singleton
public class IgnoreDAO {

    private final String SQL_DELETE_IGNORE = "DELETE FROM `ignore` WHERE `id` = ?";
    private final String SQL_GET_IGNORES_WITH_INITIATOR = "SELECT * FROM `ignore` WHERE `initiator` = ?";
    private final String SQL_GET_IGNORE_WITH_INITIATOR_AND_TARGET = "SELECT * FROM `ignore` WHERE `initiator` = ? AND `target` = ?";
    private final String SQL_INSERT_IGNORE = "INSERT INTO `ignore` (`date`, `initiator`, `target`) VALUES (?,?,?)";

    private final Database database;
    private final FLogger fLogger;

    @Inject
    public IgnoreDAO(Database database,
                     FLogger fLogger) {
        this.database = database;
        this.fLogger = fLogger;
    }

    @Nullable
    public Ignore insertIgnore(FPlayer fSender, FPlayer fIgnored) {
        if (fSender.isUnknown() || fIgnored.isUnknown()) return null;

        try (Connection connection = database.getConnection()) {
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
        } catch (SQLException e) {
            fLogger.warning(e);
        }

        return null;
    }

    @Async
    public void removeIgnore(Ignore ignore) {
        try (Connection connection = database.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_DELETE_IGNORE);
            preparedStatement.setInt(1, ignore.id());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }

    public void setIgnores(FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return;

        try (Connection connection = database.getConnection()) {
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
        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }
}
