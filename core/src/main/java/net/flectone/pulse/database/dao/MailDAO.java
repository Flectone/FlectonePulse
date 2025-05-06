package net.flectone.pulse.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Mail;
import net.flectone.pulse.util.logging.FLogger;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class MailDAO {

    private final Database database;
    private final FLogger fLogger;

    @Inject
    public MailDAO(Database database,
                   FLogger fLogger) {
        this.database = database;
        this.fLogger = fLogger;
    }

    @Nullable
    public Mail insert(FPlayer fPlayer, FPlayer fReceiver, String message) {
        if (fPlayer.isUnknown() || fReceiver.isUnknown()) return null;

        try (Connection connection = database.getConnection()) {
            String SQL_INSERT = "INSERT INTO `mail` (`date`, `sender`, `receiver`, `message`) VALUES (?,?,?,?)";
            PreparedStatement statement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

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
        } catch (SQLException e) {
            fLogger.warning(e);
        }

        return null;
    }

    public void delete(Mail mail) {
        try (Connection connection = database.getConnection()) {
            String SQL_DELETE = "DELETE FROM `mail` WHERE `id` = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_DELETE);
            preparedStatement.setInt(1, mail.id());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }

    public List<Mail> getReceiver(FPlayer fPlayer) {
        List<Mail> mails = new ArrayList<>();

        if (fPlayer.isUnknown()) return mails;

        try (Connection connection = database.getConnection()) {
            String SQL_GET_BY_RECEIVER = "SELECT * FROM `mail` WHERE `receiver` = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_BY_RECEIVER);
            preparedStatement.setInt(1, fPlayer.getId());

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                long date = resultSet.getLong("date");
                int sender = resultSet.getInt("sender");
                String message = resultSet.getString("message");

                mails.add(new Mail(id, date, sender, fPlayer.getId(), message));
            }
        } catch (SQLException e) {
            fLogger.warning(e);
        }

        return mails;
    }

    public List<Mail> getSender(FPlayer fPlayer) {
        List<Mail> mails = new ArrayList<>();

        if (fPlayer.isUnknown()) return mails;

        try (Connection connection = database.getConnection()) {
            String SQL_GET_BY_SENDER = "SELECT * FROM `mail` WHERE `sender` = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_BY_SENDER);
            preparedStatement.setInt(1, fPlayer.getId());

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                long date = resultSet.getLong("date");
                int sender = resultSet.getInt("sender");
                String message = resultSet.getString("message");

                mails.add(new Mail(id, date, sender, fPlayer.getId(), message));
            }
        } catch (SQLException e) {
            fLogger.warning(e);
        }

        return mails;
    }
}
