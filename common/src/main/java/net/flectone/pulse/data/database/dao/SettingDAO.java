package net.flectone.pulse.data.database.dao;
import java.util.HashMap;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.DatabaseImpl;
import net.flectone.pulse.data.database.sql.setting.*;
import net.flectone.pulse.data.repository.SocialRepository;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.constant.SettingText;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
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

        Map<String, Boolean> settingsBoolean = new HashMap<>();
        Map<SettingText, String> settingsText = new EnumMap<>(SettingText.class);

        withHandle(sql -> sql.findByPlayer(id)).forEach((key, value) -> {
            if (value == null) return;

            SettingText setting = SettingText.fromString(key);
            if (setting != null) {
                settingsText.put(setting, value);
                return;
            }

            settingsBoolean.put(key.toUpperCase(), "1".equals(value));
        });

        return Optional.of(new SocialRepository.Settings(Map.copyOf(settingsBoolean), Map.copyOf(settingsText)));
    }

    public void insertOrUpdate(@NonNull FPlayer player, @NonNull String setting, @Nullable String value) {
        if (database.isClosed()) return;

        useHandle(sql -> insertOrUpdate(sql, player, setting, value));
    }

    private void insertOrUpdate(SettingSQL sql, FPlayer player, String type, String value) {
        sql.upsert(player.id(), type, value);
    }

}