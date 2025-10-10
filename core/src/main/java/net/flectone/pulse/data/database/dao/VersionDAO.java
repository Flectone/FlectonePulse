package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.VersionSQL;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VersionDAO extends BaseDAO<VersionSQL> {

    private final Database database;

    @Override
    public Database database() {
        return database;
    }

    @Override
    public Class<VersionSQL> sqlClass() {
        return VersionSQL.class;
    }

    public Optional<String> find() {
        return withHandle(VersionSQL::find);
    }

    public void insert(String name) {
        useHandle(sql -> sql.insert(name));
    }

    public void update(String name) {
        useHandle(sql -> sql.update(name));
    }

    public void insertOrUpdate(String name) {
        Optional<String> versionName = find();
        if (versionName.isEmpty()) {
            insert(name);
        } else if (!versionName.get().equals(name)) {
            update(name);
        }
    }
}
