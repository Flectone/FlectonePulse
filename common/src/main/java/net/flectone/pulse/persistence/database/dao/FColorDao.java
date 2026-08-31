package net.flectone.pulse.persistence.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.value.FColor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.persistence.database.DatabaseImpl;
import net.flectone.pulse.persistence.database.sql.fcolor.*;
import org.jdbi.v3.core.mapper.Nested;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FColorDao implements BaseDAO<FColorSQL> {

    private final DatabaseImpl database;

    @Override
    public DatabaseImpl database() {
        return database;
    }

    @Override
    public Class<? extends FColorSQL> sqlClass() {
        return switch (database.config().type()) {
            case H2 -> FColorH2.class;
            case MARIADB -> FColorMariaDB.class;
            case MYSQL -> FColorMySQL.class;
            case POSTGRESQL -> FColorPostgreSQL.class;
            case SQLITE -> FColorSQLite.class;
        };
    }

    public void save(@NonNull FPlayer fPlayer, @NonNull Map<FColor.Type, Set<FColor>> colors) {
        if (database.isClosed()) return;
        if (colors.isEmpty()) {
            delete(fPlayer);
            return;
        }

        useTransaction(sql -> {
            Map<FColor.Type, Set<FColor>> oldFColors = findFColors(sql, fPlayer);
            if (colors.equals(oldFColors)) return;

            if (colors.isEmpty()) {
                sql.deleteFColors(fPlayer.id());
                return;
            }

            Arrays.stream(FColor.Type.values()).forEach(type ->
                    saveType(sql, fPlayer, type, colors.getOrDefault(type, Set.of()), oldFColors.getOrDefault(type, Set.of()))
            );
        });
    }

    public void delete(@NonNull FPlayer fPlayer) {
        if (database.isClosed()) return;

        useHandle(sql -> sql.deleteFColors(fPlayer.id()));
    }

    public Map<FColor.Type, Set<FColor>> load(@NonNull FPlayer fPlayer) {
        if (database.isClosed()) return Map.of();
        if (fPlayer.isUnknown()) return Map.of();

        return withHandle(sql -> findFColors(sql, fPlayer));
    }

    private Map<FColor.Type, Set<FColor>> findFColors(FColorSQL sql, FPlayer fPlayer) {
        return sql.findFColors(fPlayer.id()).stream()
                .collect(Collectors.groupingBy(
                        FColorInfo::type,
                        Collectors.mapping(
                                FColorInfo::fColor,
                                Collectors.toSet()
                        )
                ));
    }

    private void saveType(FColorSQL sql, FPlayer fPlayer, FColor.Type type, @NonNull Set<FColor> newFColors, @NonNull Set<FColor> oldFColors) {
        if (newFColors.equals(oldFColors)) return;
        if (newFColors.isEmpty()) {
            sql.deleteFColors(fPlayer.id(), type.name());
            return;
        }

        List<FColor> toUpsert = newFColors.stream()
                .filter(c -> !oldFColors.contains(c))
                .toList();

        Set<Integer> newNumbers = newFColors.stream()
                .map(FColor::number)
                .collect(Collectors.toSet());

        List<Integer> toDelete = oldFColors.stream()
                .map(FColor::number)
                .filter(n -> !newNumbers.contains(n))
                .toList();

        if (toUpsert.isEmpty() && toDelete.isEmpty()) return;

        if (!toUpsert.isEmpty()) {
            List<String> names = toUpsert.stream().map(FColor::name).distinct().toList();

            sql.insertFColorsIfAbsent(names);

            Map<String, Integer> nameToId = sql.findFColorIdsByNames(names);

            sql.batchUpsertPlayerFColors(
                    fPlayer.id(),
                    toUpsert.stream().map(FColor::number).toList(),
                    toUpsert.stream().map(fColor -> nameToId.get(fColor.name())).toList(),
                    type.name()
            );
        }

        if (!toDelete.isEmpty()) {
            sql.deleteFColors(fPlayer.id(), type.name(), toDelete);
        }

    }

    public record FColorInfo(
            @NonNull
            @Nested
            FColor fColor,
            FColor.@NonNull Type type
    ) {
    }

}