package net.flectone.pulse.scheduler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.util.logging.FLogger;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class FabricTaskScheduler implements TaskScheduler {

    private final AtomicLong currentTick = new AtomicLong(0L);
    private final ConcurrentSkipListMap<Long, List<ScheduledTask>> scheduledTasks = new ConcurrentSkipListMap<>();
    private ExecutorService asyncExecutor = Executors.newCachedThreadPool();
    private final FLogger logger;

    @Inject
    public FabricTaskScheduler(FLogger logger) {
        this.logger = logger;
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    @Override
    public void runAsync(RunnableException runnable) {
        asyncExecutor.execute(wrapExceptionRunnable(runnable));
    }

    @Override
    public void runAsyncTimer(RunnableException runnable, long tick, long period) {
        ScheduledTask task = new ScheduledTask(wrapExceptionRunnable(runnable), tick, period);
        registerTask(currentTick.get() + tick, task);
    }

    @Override
    public void runAsyncTimer(RunnableException runnable, long tick) {
        runAsyncTimer(runnable, tick, tick);
    }

    @Override
    public void runAsyncLater(RunnableException runnable, long tick) {
        registerTask(currentTick.get() + tick, new ScheduledTask(wrapExceptionRunnable(runnable), tick, -1));
    }

    @Override
    public void runSync(RunnableException runnable) {
        registerTask(currentTick.get(), new ScheduledTask(wrapExceptionRunnable(runnable), 0, -1));
    }

    @Override
    public void runSyncLater(RunnableException runnable, long tick) {
        registerTask(currentTick.get() + tick, new ScheduledTask(wrapExceptionRunnable(runnable), tick, -1));
    }

    @Override
    public void reload() {
        shutdown();
        asyncExecutor = Executors.newCachedThreadPool();
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
            task.runnable.run();
            if (task.isRepeating()) rescheduleTask(task);
        } catch (Exception e) {
            logger.warning("Task execution failed: " + e.getMessage());
        }
    }

    private void rescheduleTask(ScheduledTask task) {
        task.nextExecution += task.period;
        registerTask(task.nextExecution, task);
    }

    private void registerTask(long tick, ScheduledTask task) {
        scheduledTasks.compute(tick, (k, v) -> {
            if (v == null) v = new CopyOnWriteArrayList<>();
            v.add(task);
            return v;
        });
    }

    private Runnable wrapExceptionRunnable(RunnableException runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.warning("Task error: " + e.getMessage());
            }
        };
    }

    public void shutdown() {
        asyncExecutor.shutdownNow();
        scheduledTasks.clear();
    }

    private static class ScheduledTask {
        private final Runnable runnable;
        private final long period;
        private long nextExecution;
        private boolean isCanceled;

        ScheduledTask(Runnable runnable, long delay, long period) {
            this.runnable = runnable;
            this.period = period;
            this.nextExecution = System.currentTimeMillis() + delay;
        }

        boolean isRepeating() {
            return period > 0;
        }
    }
}