package net.flectone.pulse.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.database.sql.ColorsSQL;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;

import java.util.List;
import java.util.Map;

@Singleton
public class ColorsDAO extends BaseDAO<ColorsSQL> {

    private final Config.Database config;

    @Inject
    public ColorsDAO(FileResolver fileResolver, Database database) {
        super(database, ColorsSQL.class);
        this.config = fileResolver.getConfig().getDatabase();
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
                        .orElseGet(() -> config.getType() == Config.Database.Type.MYSQL
                                ? sql.upsertMySQL(colorName)
                                : sql.upsertSQLite(colorName)
                        );

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