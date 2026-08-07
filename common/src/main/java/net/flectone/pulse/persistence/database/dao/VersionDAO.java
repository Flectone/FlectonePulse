package net.flectone.pulse.persistence.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.persistence.database.DatabaseImpl;
import net.flectone.pulse.persistence.database.sql.version.*;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VersionDAO implements BaseDAO<VersionSQL> {

    private final DatabaseImpl database;

    @Override
    public DatabaseImpl database() {
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

    public Optional<String> find() {
        if (database.isClosed()) return Optional.empty();

        return withHandle(VersionSQL::find);
    }

    public void insertOrUpdate(@NonNull String name) {
        if (database.isClosed()) return;

        useHandle(sql -> sql.upsert(name));
    }

}