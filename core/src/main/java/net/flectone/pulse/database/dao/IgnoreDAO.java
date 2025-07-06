package net.flectone.pulse.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.database.sql.IgnoreSQL;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import org.jdbi.v3.core.Handle;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Singleton
public class IgnoreDAO extends BaseDAO<IgnoreSQL> {

    @Inject
    public IgnoreDAO(Database database) {
        super(database, IgnoreSQL.class);
    }

    public IgnoreSQL getSQL(Handle handle) {
        return handle.attach(IgnoreSQL.class);
    }

    @Nullable
    public Ignore insert(FPlayer fSender, FPlayer fIgnored) {
        if (fSender.isUnknown() || fIgnored.isUnknown()) return null;

        return inTransaction(sql -> {
            long currentTime = System.currentTimeMillis();
            int insertedId = sql.insert(currentTime, fSender.getId(), fIgnored.getId());
            return new Ignore(insertedId, currentTime, fIgnored.getId());
        });
    }

    public void delete(Ignore ignore) {
        useHandle(sql -> sql.delete(ignore.id()));
    }

    public void load(FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return;

        List<Ignore> ignores = withHandle(sql ->
                sql.findByInitiator(fPlayer.getId())
        );

        fPlayer.getIgnores().addAll(ignores);
    }
}