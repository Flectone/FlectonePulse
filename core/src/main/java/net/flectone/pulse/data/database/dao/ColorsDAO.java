package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.ColorsSQL;
import net.flectone.pulse.model.entity.FPlayer;

import java.util.List;
import java.util.Map;

@Singleton
public class ColorsDAO extends BaseDAO<ColorsSQL> {

    @Inject
    public ColorsDAO(Database database) {
        super(database, ColorsSQL.class);
    }

    public record ColorEntry(int number, String name) {}

    public void save(FPlayer fPlayer) {
        Map<String, String> colors = fPlayer.getColors();
        if (colors == null || colors.isEmpty()) {
            useHandle(sql -> sql.deleteAllColors(fPlayer.getId()));
            return;
        }

        useTransaction(sql -> {
            Map<Integer, Integer> currentColors = sql.getCurrentColors(fPlayer.getId());

            colors.forEach((key, colorName) -> {
                int number = Integer.parseInt(key);
                int colorId = sql.findColorIdByName(colorName)
                        .orElseGet(() -> sql.insertColor(colorName));

                if (currentColors.containsKey(number)) {
                    if (currentColors.get(number) != colorId) {
                        sql.updatePlayerColor(fPlayer.getId(), number, colorId);
                    }
                } else {
                    sql.insertPlayerColor(fPlayer.getId(), number, colorId);
                }
            });

            List<Integer> numbersToDelete = currentColors.keySet().stream()
                    .filter(num -> !colors.containsKey(String.valueOf(num)))
                    .toList();

            if (!numbersToDelete.isEmpty()) {
                sql.deletePlayerColors(fPlayer.getId(), numbersToDelete);
            }
        });
    }

    public void load(FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return;
        fPlayer.getColors().clear();

        useHandle(colorsSQL -> colorsSQL
                .loadPlayerColors(fPlayer.getId())
                .forEach(e -> fPlayer.getColors().put(String.valueOf(e.number()), e.name()))
        );
    }
}