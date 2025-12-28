package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.FColorSQL;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FPlayer;
import org.jspecify.annotations.NonNull;

import java.util.*;

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
        if (fPlayer.getFColors().isEmpty()) {
            delete(fPlayer);
            return;
        }

        useTransaction(sql ->
                Arrays.stream(FColor.Type.values()).forEach(type -> saveType(sql, fPlayer, type))
        );
    }

    /**
     * Deletes the player's color preferences from the database.
     *
     * @param fPlayer the player whose colors to delete
     */
    public void delete(@NonNull FPlayer fPlayer) {
        useHandle(sql -> sql.deleteFColors(fPlayer.getId()));
    }

    /**
     * Loads the player's color preferences from the database.
     *
     * @param fPlayer the player whose colors to load
     */
    public void load(@NonNull FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return;

        fPlayer.getFColors().clear();

        useHandle(sql ->
                Arrays.stream(FColor.Type.values()).forEach(type -> loadType(sql, fPlayer, type))
        );
    }

    private void saveType(FColorSQL sql, FPlayer fPlayer, FColor.Type type) {
        Set<FColor> newFColors = fPlayer.getFColors().getOrDefault(type, Collections.emptySet());
        Set<FColor> oldFColors = sql.findFColors(fPlayer.getId(), type.name());
        if (newFColors.equals(oldFColors)) {
            return;
        }

        if (newFColors.isEmpty()) {
            sql.deleteFColors(fPlayer.getId(), type.name());
            return;
        }

        List<Integer> fColorsToDelete = new ArrayList<>(oldFColors.stream()
                .map(FColor::number)
                .toList()
        );

        newFColors.forEach(newFColor -> {
            fColorsToDelete.remove((Integer) newFColor.number());

            Optional<FColor> optionalOldFColor = oldFColors.stream()
                    .filter(oldFColor -> oldFColor.number() == newFColor.number())
                    .findFirst();
            if (optionalOldFColor.isPresent() && optionalOldFColor.get().equals(newFColor)) return;

            int fColorId = sql.findFColorIdByName(newFColor.name()).orElseGet(() -> sql.insertFColor(newFColor.name()));

            if (optionalOldFColor.isPresent()) {
                sql.updateFColor(fPlayer.getId(), newFColor.number(), fColorId, type.name());
            } else {
                sql.insertFColor(fPlayer.getId(), newFColor.number(), fColorId, type.name());
            }
        });

        if (!fColorsToDelete.isEmpty()) {
            sql.deleteFColors(fPlayer.getId(), type.name(), fColorsToDelete);
        }
    }

    private void loadType(FColorSQL sql, FPlayer fPlayer, FColor.Type type) {
        Set<FColor> newFColors = sql.findFColors(fPlayer.getId(), type.name());
        if (!newFColors.isEmpty()) {
            fPlayer.getFColors().put(type, newFColors);
        }
    }
}