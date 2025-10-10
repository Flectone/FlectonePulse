package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.IgnoreSQL;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IgnoreDAO extends BaseDAO<IgnoreSQL> {

    private final Database database;

    @Override
    public Database database() {
        return database;
    }

    @Override
    public Class<IgnoreSQL> sqlClass() {
        return IgnoreSQL.class;
    }

    @Nullable
    public Ignore insert(FPlayer fSender, FPlayer fIgnored) {
        if (fSender.isUnknown() || fIgnored.isUnknown()) return null;

        return inTransaction(sql -> {
            long currentTime = System.currentTimeMillis();
            int updated = sql.update(currentTime, fSender.getId(), fIgnored.getId());

            if (updated == 0) {
                int insertedId = sql.insert(currentTime, fSender.getId(), fIgnored.getId());
                return new Ignore(insertedId, currentTime, fIgnored.getId());
            } else {
                return sql.findByInitiatorAndTarget(fSender.getId(), fIgnored.getId()).orElseThrow();
            }
        });
    }

    public void invalidate(Ignore ignore) {
        useHandle(sql -> sql.invalidate(ignore.id()));
    }

    public void load(FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return;

        List<Ignore> ignores = withHandle(sql ->
                sql.findByInitiator(fPlayer.getId())
        );

        fPlayer.getIgnores().addAll(ignores);
    }
}