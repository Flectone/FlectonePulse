package net.flectone.pulse.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.logging.FLogger;

import java.sql.*;
import java.util.Map;

@Singleton
public class ColorsDAO {

    private final Config.Database config;
    private final Database database;
    private final FLogger fLogger;

    @Inject
    public ColorsDAO(FileManager fileManager,
                     Database database,
                     FLogger fLogger) {
        this.config = fileManager.getConfig().getDatabase();
        this.database = database;
        this.fLogger = fLogger;
    }

    public void save(FPlayer fPlayer) {
        try (Connection connection = database.getConnection()) {
            Map<String, String> colors = fPlayer.getColors();

            String SQL_DELETE_BY_ID = "DELETE FROM `player_color` WHERE `player` = ?";
            if (colors == null) {
                PreparedStatement preparedStatement = connection.prepareStatement(SQL_DELETE_BY_ID);
                preparedStatement.setInt(1, fPlayer.getId());
                preparedStatement.executeUpdate();
                return;
            }

            if (colors.isEmpty()) {
                PreparedStatement preparedStatement = connection.prepareStatement(SQL_DELETE_BY_ID);
                preparedStatement.setInt(1, fPlayer.getId());
                preparedStatement.executeUpdate();
                return;
            }

            for (Map.Entry<String, String> entry : colors.entrySet()) {
                String SQL_GET_BY_NAME = "SELECT * FROM `color` WHERE `name` = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_BY_NAME);
                preparedStatement.setString(1, String.valueOf(entry.getValue()));

                ResultSet colorResult = preparedStatement.executeQuery();

                int color = -1;
                if (colorResult.next()) {
                    color = colorResult.getInt("id");
                } else {
                    String SQLITE_INSERT_OR_UPDATE = "INSERT INTO `color` (`name`) VALUES (?) ON CONFLICT(`name`) DO UPDATE SET `name` = excluded.`name`";
                    String MYSQL_INSERT_OR_UPDATE = "INSERT INTO `color` (`name`) VALUES (?) ON DUPLICATE KEY UPDATE `name` = VALUES(`name`)";
                    preparedStatement = connection.prepareStatement(config.getType() == Config.Database.Type.MYSQL
                            ? MYSQL_INSERT_OR_UPDATE
                            : SQLITE_INSERT_OR_UPDATE, Statement.RETURN_GENERATED_KEYS);
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

                String SQL_DELETE_BY_NUMBER_AND_SETTING = "DELETE FROM `player_color` WHERE `number` = ? AND `player` = ?";
                preparedStatement = connection.prepareStatement(SQL_DELETE_BY_NUMBER_AND_SETTING);
                preparedStatement.setInt(1, Integer.parseInt(entry.getKey()));
                preparedStatement.setInt(2, fPlayer.getId());
                preparedStatement.executeUpdate();

                String SQLITE_INSERT_OR_UPDATE = "INSERT OR REPLACE INTO `player_color` (`number`, `player`, `color`) VALUES (?,?,?) ";
                String MYSQL_INSERT_OR_UPDATE = "INSERT INTO `player_color` (`number`, `player`, `color`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE `number` = VALUES(`number`), `player` = VALUES(`player`), `color` = VALUES(`color`)";
                preparedStatement = connection.prepareStatement(config.getType() == Config.Database.Type.MYSQL ? MYSQL_INSERT_OR_UPDATE : SQLITE_INSERT_OR_UPDATE);
                preparedStatement.setInt(1, Integer.parseInt(entry.getKey()));
                preparedStatement.setInt(2, fPlayer.getId());
                preparedStatement.setInt(3, color);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }

    public void load(FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return;
        fPlayer.getColors().clear();

        try (Connection connection = database.getConnection()) {
            String SQL_GET_BY_PLAYER_ID = "SELECT * FROM `player_color` LEFT JOIN `color` ON `player_color`.`color` = `color`.`id` WHERE `player_color`.`player` = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_BY_PLAYER_ID);
            preparedStatement.setInt(1, fPlayer.getId());

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String number = String.valueOf(resultSet.getInt("number"));
                String color = resultSet.getString("name");

                fPlayer.getColors().put(number, color);
            }
        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }
}
