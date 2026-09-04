package net.flectone.pulse.persistence.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.persistence.database.DatabaseImpl;
import net.flectone.pulse.persistence.database.sql.setting.*;
import net.flectone.pulse.persistence.repository.SocialRepository;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SettingDAO implements BaseDAO<SettingSQL> {

    private final DatabaseImpl database;

    @Override
    public DatabaseImpl database() {
        return database;
    }

    @Override
    public Class<? extends SettingSQL> sqlClass() {
        return switch (database.config().type()) {
            case H2 -> SettingH2.class;
            case MARIADB -> SettingMariaDB.class;
            case MYSQL -> SettingMySQL.class;
            case POSTGRESQL -> SettingPostgreSQL.class;
            case SQLITE -> SettingSQLite.class;
        };
    }

    public Optional<SocialRepository.Settings> load(@NonNull FPlayer player) {
        if (database.isClosed()) return Optional.empty();

        int id = player.id();

        Map<String, String> values = withHandle(sql -> sql.findByPlayer(id));

        return Optional.of(new SocialRepository.Settings(Collections.unmodifiableMap(values)));
    }

    public void insertOrUpdate(@NonNull FPlayer player, @NonNull String setting, @Nullable String value) {
        if (database.isClosed()) return;
        if (player.isUnknown()) return;

        useHandle(sql -> insertOrUpdate(sql, player, setting, value));
    }

    private void insertOrUpdate(SettingSQL sql, FPlayer player, String type, String value) {
        sql.upsert(player.id(), type, value);
    }

}