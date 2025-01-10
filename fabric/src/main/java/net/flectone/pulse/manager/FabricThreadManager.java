package net.flectone.pulse.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.database.DatabaseConsumer;
import net.flectone.pulse.logger.FLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Singleton
public class FabricThreadManager extends ThreadManager {

    private long ticks = 0L;
    private final HashMap<Long, List<BlfRunnable>> runnableMap = new HashMap<>();


    private final Database database;
    private final FLogger fLogger;

    @Inject
    public FabricThreadManager(Database database,
                               FLogger fLogger) {
        super(database, fLogger);

        this.database = database;
        this.fLogger = fLogger;
    }

    /**
     * Runs a task after specified amount of ticks.
     * @param delay Number of ticks before the task is run. Should be at least 0.
     * @param runnable Task being run.
     * @return Runnable specified as parameter.
     */
    public BlfRunnable delay(long delay, @NotNull BlfRunnable runnable) {
        delay = properDelayCheck(delay);

        runnable.setCancelled(false);
        runnable.setRepeating(false);
        runnable.setPeriod(Integer.MAX_VALUE);

        long time = ticks + delay;
        addTask(time, runnable);

        return runnable;
    }

    /**
     * Repeats a task every specified amount of ticks.
     * @param delay Number of ticks before the first task is run. Should be at least 0.
     * @param period Number of ticks between following runs. Should be at least 1.
     * @param runnable Task being repeated.
     * @return Runnable specified as parameter.
     */
    public BlfRunnable repeat(long delay, long period, @NotNull BlfRunnable runnable) {
        delay = properDelayCheck(delay);

        runnable.setCancelled(false);
        runnable.setRepeating(true);
        runnable.setPeriod(period);

        long time = ticks + delay;
        addTask(time, runnable);

        return runnable;
    }

    public void tick() {
        List<BlfRunnable> currentTasks = runnableMap.remove(ticks);
        if (currentTasks != null) {
            for (BlfRunnable runnable : currentTasks) {
                runCurrentTimeTask(runnable);
            }
        }
        ticks++;
    }

    private void runCurrentTimeTask(BlfRunnable runnable) {
        if (runnable.isCancelled()) return;
        runnable.run();
        if (runnable.isCancelled() || !runnable.isRepeating()) return;

        Long period = properPeriodCheck(runnable.getPeriod());
        if (period == null) return;
        runnable.setPeriod(period);

        long time = ticks + runnable.getPeriod();
        addTask(time, runnable);
    }

    private void addTask(long time, BlfRunnable runnable) {
        List<BlfRunnable> runnables = runnableMap.get(time);

        if (runnables == null) {
            runnables = new ArrayList<>(1);
            runnables.add(runnable);
            runnableMap.put(time, runnables);
        } else {
            runnables.add(runnable);
        }
    }

    private long properDelayCheck(long delay) {
        if (delay < 0) {
            delay = 0;
        }
        return delay;
    }

    @Nullable
    private Long properPeriodCheck(@NotNull Long period) {
        if (period < 0) {
            period = null;
        } else if (period == 0) {
            period = 1L;
        }
        return period;
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
        repeat(tick, period, new BlfRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }

    @Override
    public void runAsyncLater(Runnable runnable, long tick) {
        delay(tick, new BlfRunnable() {
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
        delay(tick, new BlfRunnable() {
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
