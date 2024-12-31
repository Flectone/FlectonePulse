package net.flectone.pulse.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.database.DatabaseConsumer;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.scheduler.BlfRunnable;
import net.flectone.pulse.scheduler.BlfScheduler;

import java.sql.SQLException;

@Singleton
public class FabricThreadManager extends ThreadManager {


    private final Database database;
    private final FLogger fLogger;

    @Inject
    public FabricThreadManager(Database database,
                               FLogger fLogger) {
        super(database, fLogger);

        this.database = database;
        this.fLogger = fLogger;
    }

    @Override
    public void runAsync(DatabaseConsumer databaseAction) {
        runAsync(() -> {
            try {
                databaseAction.accept(database);
            } catch (SQLException e) {
                fLogger.warning(e);
            }
        });
    }

    @Override
    public void runAsync(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void runAsyncTimer(Runnable runnable, long tick, long period) {
        BlfScheduler.repeat(tick, period, new BlfRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }

    @Override
    public void runAsyncLater(Runnable runnable, long tick) {
        BlfScheduler.delay(tick, new BlfRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }

    @Override
    public void runSync(Runnable runnable) {
        runnable.run();
    }

    public void runSyncLater(Runnable runnable, long tick) {
        BlfScheduler.delay(tick, new BlfRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }

    @Override
    public void reload() {

    }
}
