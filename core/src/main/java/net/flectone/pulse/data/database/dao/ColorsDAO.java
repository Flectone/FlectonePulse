package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.FColorSQL;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FPlayer;
import org.jdbi.v3.core.mapper.Nested;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Data Access Object for color data operations in FlectonePulse.
 * Handles persistence and retrieval of player color preferences.
 *
 * @author TheFaser
 * @since 0.9.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ColorsDAO implements BaseDAO<FColorSQL> {

    private final Database database;

    @Override
    public Database database() {
        return database;
    }

    @Override
    public Class<FColorSQL> sqlClass() {
        return FColorSQL.class;
    }

    /**
     * Saves the player's color preferences to the database.
     *
     * @param fPlayer the player whose colors to save
     */
    public void save(@NonNull FPlayer fPlayer) {
        if (fPlayer.fColors().isEmpty()) {
            delete(fPlayer);
            return;
        }

        useTransaction(sql -> {
            Map<FColor.Type, Set<FColor>> newFColors = fPlayer.fColors();
            Map<FColor.Type, Set<FColor>> oldFColors = findFColors(sql, fPlayer);
            if (newFColors.equals(oldFColors) || newFColors.isEmpty() && oldFColors.isEmpty()) return;

            if (newFColors.isEmpty()) {
                sql.deleteFColors(fPlayer.id());
                return;
            }

            Arrays.stream(FColor.Type.values()).forEach(type ->
                    saveType(sql, fPlayer, type, newFColors.getOrDefault(type, Collections.emptySet()), oldFColors.getOrDefault(type, Collections.emptySet()))
            );
        });
    }

    /**
     * Deletes the player's color preferences from the database.
     *
     * @param fPlayer the player whose colors to delete
     */
    public void delete(@NonNull FPlayer fPlayer) {
        useHandle(sql -> sql.deleteFColors(fPlayer.id()));
    }

    /**
     * Loads the player's color preferences from the database.
     *
     * @param fPlayer the player whose colors to load
     * @return new FPlayer with colors
     */
    public FPlayer load(@NonNull FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return fPlayer;

        return withHandle(sql -> fPlayer.withFColors(findFColors(sql, fPlayer)));
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

        IntArrayList fColorsToDelete = new IntArrayList(oldFColors.stream()
                .map(FColor::number)
                .toList()
        );

        newFColors.forEach(newFColor -> {
            fColorsToDelete.rem(newFColor.number());

            Optional<FColor> optionalOldFColor = oldFColors.stream()
                    .filter(oldFColor -> oldFColor.number() == newFColor.number())
                    .findAny();
            if (optionalOldFColor.isPresent() && optionalOldFColor.get().equals(newFColor)) return;

            int fColorId = sql.findFColorIdByName(newFColor.name()).orElseGet(() -> sql.insertFColor(newFColor.name()));

            if (optionalOldFColor.isPresent()) {
                sql.updateFColor(fPlayer.id(), newFColor.number(), fColorId, type.name());
            } else {
                sql.insertFColor(fPlayer.id(), newFColor.number(), fColorId, type.name());
            }
        });

        if (!fColorsToDelete.isEmpty()) {
            sql.deleteFColors(fPlayer.id(), type.name(), fColorsToDelete);
        }
    }

    /**
     * Represents colors information retrieved from the database.
     *
     * @param fColor the color instance containing color details
     * @param type the classification type of the color
     */
    public record FColorInfo(
            @NonNull
            @Nested
            FColor fColor,

            FColor.@NonNull Type type
    ){}

}