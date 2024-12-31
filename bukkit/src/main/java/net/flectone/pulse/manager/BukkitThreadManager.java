package net.flectone.pulse.manager;

import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.database.DatabaseConsumer;
import net.flectone.pulse.logger.FLogger;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;

@Singleton
public class BukkitThreadManager extends ThreadManager {

    private final Plugin plugin;
    private final TaskScheduler taskScheduler;
    private final Database database;
    private final FLogger fLogger;

    @Inject
    public BukkitThreadManager(Plugin plugin,
                               TaskScheduler taskScheduler,
                               Database database,
                               FLogger fLogger) {
        super(database, fLogger);
        this.plugin = plugin;
        this.taskScheduler = taskScheduler;
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
        taskScheduler.runTaskAsynchronously(runnable);
    }

    @Override
    public void runAsyncTimer(Runnable runnable, long tick, long period) {
        taskScheduler.runTaskTimerAsynchronously(runnable, tick, period);
    }

    @Override
    public void runAsyncLater(Runnable runnable, long tick) {
        taskScheduler.runTaskLaterAsynchronously(runnable, tick);
    }

    @Override
    public void runSync(Runnable runnable) {
        taskScheduler.runTask(runnable);
    }

    public void runSyncLater(Runnable runnable, long tick) {
        taskScheduler.runTaskLater(runnable, tick);
    }

    @Override
    public void reload() {
        taskScheduler.cancelTasks(plugin);
    }
}
