package net.flectone.pulse.database;

import java.sql.SQLException;

@FunctionalInterface
public interface DatabaseConsumer {
    void accept(Database database) throws SQLException;
}
