package net.flectone.pulse.manager;

import net.flectone.pulse.database.Database;
import net.flectone.pulse.database.DatabaseConsumer;
import net.flectone.pulse.logger.FLogger;

import java.sql.SQLException;

public abstract class ThreadManager {

    private final Database database;
    private final FLogger fLogger;

    public ThreadManager(Database database,
                         FLogger fLogger) {
        this.database = database;
        this.fLogger = fLogger;
    }

    public void runDatabase(Runnable runnable) {
        database.execute(runnable);
    }

    public void runDatabase(DatabaseConsumer databaseAction) {
        runDatabase(() -> {
            try {
                databaseAction.accept(database);
            } catch (SQLException e) {
                fLogger.warning(e);
            }
        });
    }

    public void runAsync(DatabaseConsumer databaseAction) {
        runAsync(() -> {
            try {
                databaseAction.accept(database);
            } catch (SQLException e) {
                fLogger.warning(e);
            }
        });
    }

    public abstract void runAsync(Runnable runnable);
    public abstract void runAsyncTimer(Runnable runnable, long tick, long period);
    public abstract void runAsyncLater(Runnable runnable, long tick);
    public abstract void runSync(Runnable runnable);
    public abstract void runSyncLater(Runnable runnable, long tick);
    public abstract void reload();
}
