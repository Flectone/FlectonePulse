package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.version.*;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 * Data Access Object for version data in FlectonePulse.
 * Handles storage and retrieval of plugin version information in the database.
 *
 * @author TheFaser
 * @since 1.6.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VersionDAO implements BaseDAO<VersionSQL> {

    private final Database database;

    @Override
    public Database database() {
        return database;
    }

    @Override
    public Class<? extends VersionSQL> sqlClass() {
        return switch (database.config().type()) {
            case H2 -> VersionH2.class;
            case MARIADB -> VersionMariaDB.class;
            case MYSQL -> VersionMySQL.class;
            case POSTGRESQL -> VersionPostgreSQL.class;
            case SQLITE -> VersionSQLite.class;
        };
    }

    /**
     * Finds the stored version name.
     *
     * @return optional containing the version name if found
     */
    public Optional<String> find() {
        if (database.isClosed()) return Optional.empty();

        return withHandle(VersionSQL::find);
    }

    /**
     * Inserts or updates the version name.
     *
     * @param name the version name
     */
    public void insertOrUpdate(@NonNull String name) {
        if (database.isClosed()) return;

        useHandle(sql -> sql.upsert(name));
    }

}