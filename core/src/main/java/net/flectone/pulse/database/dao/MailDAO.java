package net.flectone.pulse.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.command.mail.model.Mail;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class MailDAO {

    private final String SQL_DELETE_MAIL = "DELETE FROM `mail` WHERE `id` = ?";
    private final String SQL_GET_MAILS_WITH_RECEIVER = "SELECT * FROM `mail` WHERE `receiver` = ?";
    private final String SQL_INSERT_MAIL = "INSERT INTO `mail` (`date`, `sender`, `receiver`, `message`) VALUES (?,?,?,?)";

    private final Database database;
    private final FLogger fLogger;

    @Inject
    public MailDAO(Database database,
                   FLogger fLogger) {
        this.database = database;
        this.fLogger = fLogger;
    }

    @Nullable
    public Mail insertMail(FPlayer fPlayer, FPlayer fReceiver, String message) {
        if (fPlayer.isUnknown() || fReceiver.isUnknown()) return null;

        try (Connection connection = database.getConnection()) {
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
        } catch (SQLException e) {
            fLogger.warning(e);
        }

        return null;
    }

    @Async
    public void removeMail(Mail mail) {
        try (Connection connection = database.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_DELETE_MAIL);
            preparedStatement.setInt(1, mail.id());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }

    public List<Mail> getMails(FPlayer fPlayer) {
        List<Mail> mails = new ArrayList<>();

        if (fPlayer.isUnknown()) return mails;

        try (Connection connection = database.getConnection()) {
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
        } catch (SQLException e) {
            fLogger.warning(e);
        }

        return mails;
    }
}
