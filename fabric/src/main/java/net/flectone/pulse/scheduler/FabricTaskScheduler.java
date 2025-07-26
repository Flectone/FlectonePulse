package net.flectone.pulse.scheduler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.util.logging.FLogger;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class FabricTaskScheduler implements TaskScheduler {

    private final AtomicLong currentTick = new AtomicLong(0L);
    private final ConcurrentSkipListMap<Long, List<ScheduledTask>> scheduledTasks = new ConcurrentSkipListMap<>();
    private final FLogger logger;

    private ExecutorService asyncExecutor;

    @Inject
    public FabricTaskScheduler(FLogger logger) {
        this.logger = logger;

        createExecutorService();

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    @Override
    public void runAsync(SchedulerRunnable runnable) {
        asyncExecutor.execute(wrapExceptionRunnable(runnable));
    }

    @Override
    public void runAsyncTimer(SchedulerRunnable runnable, long tick, long period) {
        long firstTick = currentTick.get() + tick;
        ScheduledTask task = new ScheduledTask(wrapExceptionRunnable(runnable), firstTick, period, true);
        registerTask(firstTick, task);
    }

    @Override
    public void runAsyncTimer(SchedulerRunnable runnable, long tick) {
        runAsyncTimer(runnable, tick, tick);
    }

    @Override
    public void runAsyncLater(SchedulerRunnable runnable, long tick) {
        long firstTick = currentTick.get() + tick;
        ScheduledTask task = new ScheduledTask(wrapExceptionRunnable(runnable), firstTick, -1, true);
        registerTask(firstTick, task);
    }

    @Override
    public void runSync(SchedulerRunnable runnable) {
        long firstTick = currentTick.get();
        ScheduledTask task = new ScheduledTask(wrapExceptionRunnable(runnable), firstTick, -1, false);
        registerTask(firstTick, task);
    }

    @Override
    public void runSyncRegion(Object entity, SchedulerRunnable runnable) {
        runSync(runnable);
    }

    @Override
    public void runSyncTimer(SchedulerRunnable runnable, long tick, long period) {
        long firstTick = currentTick.get() + tick;
        ScheduledTask task = new ScheduledTask(wrapExceptionRunnable(runnable), firstTick, period, false);
        registerTask(firstTick, task);
    }

    @Override
    public void runSyncTimer(SchedulerRunnable runnable, long tick) {
        runSyncTimer(runnable, tick, tick);
    }

    @Override
    public void runSyncLater(SchedulerRunnable runnable, long tick) {
        long firstTick = currentTick.get() + tick;
        ScheduledTask task = new ScheduledTask(wrapExceptionRunnable(runnable), firstTick, -1, false);
        registerTask(firstTick, task);
    }

    @Override
    public void reload() {
        shutdown();
        createExecutorService();
        scheduledTasks.clear();
        currentTick.set(0L);
    }

    public void onTick() {
        long tick = currentTick.getAndIncrement();
        processTasks(tick);
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

    private Runnable wrapExceptionRunnable(SchedulerRunnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.warning("Task error: " + e.getMessage());
            }
        };
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

        this.asyncExecutor = Executors.newCachedThreadPool(namedThreadFactory);
    }

    public void shutdown() {
        asyncExecutor.shutdownNow();
        scheduledTasks.clear();
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