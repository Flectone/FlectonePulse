package net.flectone.pulse.execution.scheduler;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.exception.SchedulerTaskException;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.logging.FLogger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Singleton
public class FabricTaskScheduler implements TaskScheduler {

    private final AtomicLong currentTick = new AtomicLong(0L);
    private final Map<Long, List<ScheduledTask>> scheduledTasks = new ConcurrentSkipListMap<>();
    private final FLogger logger;
    private final Provider<FPlayerService> fPlayerServiceProvider;
    private final Provider<PlatformServerAdapter> platformServerAdapterProvider;

    private ExecutorService asyncExecutor;
    private volatile boolean disabled = false;

    @Inject
    public FabricTaskScheduler(FLogger logger,
                               Provider<FPlayerService> fPlayerServiceProvider,
                               Provider<PlatformServerAdapter> platformServerAdapterProvider) {
        this.logger = logger;
        this.fPlayerServiceProvider = fPlayerServiceProvider;
        this.platformServerAdapterProvider = platformServerAdapterProvider;

        createExecutorService();
    }

    @Override
    public void shutdown() {
        disabled = true;

        asyncExecutor.shutdown();

        try {
            if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncExecutor.shutdownNow();
        }

        scheduledTasks.clear();
    }

    @Override
    public void reload() {
        shutdown();

        createExecutorService();
        scheduledTasks.clear();
        currentTick.set(0L);

        disabled = false;
    }

    @Override
    public void runAsync(SchedulerRunnable runnable, boolean independent) {
        if (disabled) return;

        if (!independent && isAsyncThread()) {
            wrapExceptionRunnable(runnable).run();
            return;
        }

        asyncExecutor.execute(wrapExceptionRunnable(runnable));
    }

    @Override
    public void runAsyncLater(SchedulerRunnable runnable, long delay) {
        if (disabled) return;

        long firstTick = currentTick.get() + delay;
        ScheduledTask task = new ScheduledTask(wrapExceptionRunnable(runnable), firstTick, -1, true);
        registerTask(firstTick, task);
    }

    @Override
    public void runAsyncTimer(SchedulerRunnable runnable, long delay, long period) {
        if (disabled) return;

        long firstTick = currentTick.get() + delay;
        ScheduledTask task = new ScheduledTask(wrapExceptionRunnable(runnable), firstTick, period, true);
        registerTask(firstTick, task);
    }

    @Override
    public void runSync(SchedulerRunnable runnable) {
        if (disabled) return;

        if (!isAsyncThread()) {
            wrapExceptionRunnable(runnable).run();
            return;
        }

        long firstTick = currentTick.get();
        ScheduledTask task = new ScheduledTask(wrapExceptionRunnable(runnable), firstTick, -1, false);
        registerTask(firstTick, task);
    }

    @Override
    public void runSyncLater(SchedulerRunnable runnable, long delay) {
        if (disabled) return;

        long firstTick = currentTick.get() + delay;
        ScheduledTask task = new ScheduledTask(wrapExceptionRunnable(runnable), firstTick, -1, false);
        registerTask(firstTick, task);
    }

    @Override
    public void runSyncTimer(SchedulerRunnable runnable, long delay, long period) {
        if (disabled) return;

        long firstTick = currentTick.get() + delay;
        ScheduledTask task = new ScheduledTask(wrapExceptionRunnable(runnable), firstTick, period, false);
        registerTask(firstTick, task);
    }

    @Override
    public void runRegion(FPlayer fPlayer, SchedulerRunnable runnable, boolean sync) {
        runAsync(runnable);
    }

    @Override
    public void runRegionLater(FPlayer fPlayer, SchedulerRunnable runnable, long delay) {
        runAsyncLater(runnable, delay);
    }

    @Override
    public void runRegionTimer(FPlayer fPlayer, SchedulerRunnable runnable, long delay, long period) {
        runAsyncTimer(runnable, delay, period);
    }

    @Override
    public void runPlayerRegionTimer(Consumer<FPlayer> fPlayerConsumer, long delay) {
        if (disabled) return;

        runAsyncTimer(() -> {
            for (FPlayer fPlayer : fPlayerServiceProvider.get().getOnlineFPlayers()) {
                runAsync(() -> fPlayerConsumer.accept(fPlayer));
            }
        }, delay);
    }

    @Override
    public Runnable wrapExceptionRunnable(SchedulerRunnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (SchedulerTaskException e) {
                logger.warning(e);
            }
        };
    }

    public void onTick() {
        if (disabled) return;

        long tick = currentTick.getAndIncrement();
        processTasks(tick);
    }

    private boolean isAsyncThread() {
        return !platformServerAdapterProvider.get().isPrimaryThread() && !isRestrictedAsyncThread();
    }

    private void processTasks(long tick) {
        List<ScheduledTask> tasks = scheduledTasks.get(tick);
        if (tasks == null) return;

        tasks.removeIf(task -> {
            if (task.isCanceled) return true;
            executeTask(task);
            return true;
        });
    }

    private void executeTask(ScheduledTask task) {
        try {
            if (task.isAsync) {
                asyncExecutor.execute(task.runnable);
            } else {
                task.runnable.run();
            }
            if (task.isRepeating()) rescheduleTask(task);
        } catch (Exception e) {
            logger.warning("Task execution failed: " + e.getMessage());
        }
    }

    private void rescheduleTask(ScheduledTask task) {
        if (task.isRepeating()) {
            task.nextTick += task.period;
            registerTask(task.nextTick, task);
        }
    }

    private void registerTask(long tick, ScheduledTask task) {
        scheduledTasks.compute(tick, (k, v) -> {
            if (v == null) v = new CopyOnWriteArrayList<>();
            v.add(task);
            return v;
        });
    }

    private void createExecutorService() {
        ThreadFactory namedThreadFactory = new ThreadFactory() {
            private final AtomicLong threadCounter = new AtomicLong(0);

            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setName("FlectonePulseThread-" + threadCounter.incrementAndGet());
                return thread;
            }
        };

        this.asyncExecutor = Executors.newFixedThreadPool(8, namedThreadFactory);
    }

    private static class ScheduledTask {
        private final Runnable runnable;
        private final long period;
        private long nextTick;
        private final boolean isAsync;
        private boolean isCanceled;

        ScheduledTask(Runnable runnable, long firstTick, long period, boolean isAsync) {
            this.runnable = runnable;
            this.nextTick = firstTick;
            this.period = period;
            this.isAsync = isAsync;
        }

        boolean isRepeating() {
            return period > 0;
        }
    }
}
