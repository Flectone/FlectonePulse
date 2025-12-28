package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.VersionSQL;
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
    public Class<VersionSQL> sqlClass() {
        return VersionSQL.class;
    }

    /**
     * Finds the stored version name.
     *
     * @return optional containing the version name if found
     */
    public Optional<String> find() {
        return withHandle(VersionSQL::find);
    }

    /**
     * Inserts a new version name.
     *
     * @param name the version name to insert
     */
    public void insert(@NonNull String name) {
        useHandle(sql -> sql.insert(name));
    }

    /**
     * Updates the version name.
     *
     * @param name the new version name
     */
    public void update(@NonNull String name) {
        useHandle(sql -> sql.update(name));
    }

    /**
     * Inserts or updates the version name.
     *
     * @param name the version name
     */
    public void insertOrUpdate(@NonNull String name) {
        Optional<String> versionName = find();
        if (versionName.isEmpty()) {
            insert(name);
        } else if (!versionName.get().equals(name)) {
            update(name);
        }
    }

}