package net.flectone.pulse.execution.scheduler;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.With;
import net.flectone.pulse.FlectonePulseAPI;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * A task scheduler that manages asynchronous and synchronous task execution
 * with tick-based scheduling support.
 * <p>
 * This scheduler provides methods for running tasks immediately, with delays, or on repeating intervals.
 * It supports both async (thread pool) and sync (current thread) execution modes.
 * Tasks are scheduled using a tick-based system where each tick represents a scheduling unit.
 *
 * @author TheFaser
 * @since 0.5.3
 */
@Singleton
public class TaskScheduler {

    private static final String THREAD_PREFIX = "FlectonePulse Thread #";

    private final AtomicLong currentTick = new AtomicLong(0L);
    private final AtomicLong threadCounter = new AtomicLong(0L);
    private final Map<Long, List<ScheduledTask>> scheduledTasks = new ConcurrentSkipListMap<>();
    private final FLogger fLogger;
    private final Provider<FPlayerService> fPlayerServiceProvider;

    @Inject
    private Provider<FileFacade> fileFacadeProvider;

    @Getter
    private ExecutorService executorService;
    private Config.Executor config;

    private volatile boolean disabled = false;

    @Inject
    public TaskScheduler(FLogger fLogger,
                         Provider<FPlayerService> fPlayerServiceProvider) {
        this.fLogger = fLogger;
        this.fPlayerServiceProvider = fPlayerServiceProvider;
    }

    /**
     * Reloads the scheduler by processing all pending tasks at the current tick,
     * clearing all scheduled tasks, and resetting the tick counter to zero.
     */
    public void reload() {
        processTasks(currentTick.get());
        scheduledTasks.clear();
        currentTick.set(0L);
    }

    /**
     * Starts the scheduler by creating and initializing the executor service
     * with the configured thread pool settings.
     */
    public void start() {
        executorService = createExecutorService();
    }

    /**
     * Shuts down the scheduler gracefully by disabling new task submissions,
     * processing remaining tasks at the current tick, and waiting for
     * executing tasks to complete within the configured timeout period.
     * If tasks don't complete within the timeout, forces immediate shutdown.
     */
    public void shutdown() {
        disabled = true;

        processTasks(currentTick.get());

        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(config.shutdownTimeout().duration(), config.shutdownTimeout().timeUnit())) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            fLogger.warning(e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            scheduledTasks.clear();
        }
    }

    /**
     * Schedules a task to run asynchronously on the thread pool.
     *
     * @param runnable the task to execute
     * @return a CompletableFuture that completes when the task finishes
     */
    public CompletableFuture<Void> runAsync(SchedulerRunnable runnable) {
        return runAsync(runnable, false);
    }

    /**
     * Schedules a task to run asynchronously with control over independent execution.
     * If not independent and already on an async thread, runs immediately to avoid unnecessary threading.
     *
     * @param runnable the task to execute
     * @param independent if true, always schedules even if already on async thread; if false, may run immediately
     * @return a CompletableFuture that completes when the task finishes
     */
    public CompletableFuture<Void> runAsync(SchedulerRunnable runnable, boolean independent) {
        if (isDisabled()) return runImmediately(runnable);

        if (!independent && isAsyncThread()) {
            return runImmediately(runnable);
        }

        // we don't need to create a task to do this in async
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        execute(wrapExceptionRunnable(runnable, completableFuture));

        return completableFuture;
    }

    /**
     * Schedules a task to run asynchronously after a default delay of 20 ticks.
     *
     * @param runnable the task to execute
     * @return a CompletableFuture that completes when the task finishes
     */
    public CompletableFuture<Void> runAsyncLater(SchedulerRunnable runnable) {
        return runAsyncLater(runnable, 20L);
    }

    /**
     * Schedules a task to run asynchronously after the specified delay.
     *
     * @param runnable the task to execute
     * @param delay the number of ticks to wait before executing the task
     * @return a CompletableFuture that completes when the task finishes
     */
    public CompletableFuture<Void> runAsyncLater(SchedulerRunnable runnable, long delay) {
        if (isDisabled()) return runImmediately(runnable);

        long firstTick = currentTick.get() + delay;
        return registerTask(runnable, firstTick, -1L, true).future();
    }

    /**
     * Schedules a task to run asynchronously on a repeating interval with a default initial delay of 5 ticks.
     *
     * @param runnable the task to execute repeatedly
     * @param period the number of ticks between consecutive executions
     * @return a CompletableFuture that completes when the task is first executed
     */
    public CompletableFuture<Void> runAsyncTimer(SchedulerRunnable runnable, long period) {
        return runAsyncTimer(runnable, 5L, period);
    }

    /**
     * Schedules a task to run asynchronously on a repeating interval with custom delay and period.
     *
     * @param runnable the task to execute repeatedly
     * @param delay the number of ticks to wait before the first execution
     * @param period the number of ticks between consecutive executions
     * @return a CompletableFuture that completes when the task is first executed
     */
    public CompletableFuture<Void> runAsyncTimer(SchedulerRunnable runnable, long delay, long period) {
        if (isDisabled()) return runImmediately(runnable);

        long firstTick = currentTick.get() + delay;
        return registerTask(runnable, firstTick, period, true).future();
    }

    /**
     * Schedules a task to run synchronously on the current thread.
     *
     * @param runnable the task to execute
     * @return a CompletableFuture that completes when the task finishes
     */
    public CompletableFuture<Void> runSync(SchedulerRunnable runnable) {
        if (isDisabled()) return runImmediately(runnable);

        return registerTask(runnable, currentTick.get(), -1L, false).future();
    }

    /**
     * Schedules a task to run synchronously on the current thread after a specified delay.
     *
     * @param runnable the task to execute
     * @param delay the number of ticks to wait before executing the task
     * @return a CompletableFuture that completes when the task finishes
     */
    public CompletableFuture<Void> runSyncLater(SchedulerRunnable runnable, long delay) {
        if (isDisabled()) return runImmediately(runnable);

        long firstTick = currentTick.get() + delay;
        return registerTask(runnable, firstTick, -1L, false).future();
    }

    /**
     * Schedules a task to run synchronously on a repeating interval with a default initial delay of 5 ticks.
     *
     * @param runnable the task to execute repeatedly
     * @param period the number of ticks between consecutive executions
     * @return a CompletableFuture that completes when the task is first executed
     */
    public CompletableFuture<Void> runSyncTimer(SchedulerRunnable runnable, long period) {
        return runSyncTimer(runnable, 5L, period);
    }

    /**
     * Schedules a task to run synchronously on a repeating interval with custom delay and period.
     *
     * @param runnable the task to execute repeatedly
     * @param delay the number of ticks to wait before the first execution
     * @param period the number of ticks between consecutive executions
     * @return a CompletableFuture that completes when the task is first executed
     */
    public CompletableFuture<Void> runSyncTimer(SchedulerRunnable runnable, long delay, long period) {
        if (isDisabled()) return runImmediately(runnable);

        long firstTick = currentTick.get() + delay;
        return registerTask(runnable, firstTick, period, false).future();
    }

    /**
     * Schedules a region-related task to run asynchronously.
     *
     * @param fPlayer the player associated with the region (currently unused)
     * @param runnable the task to execute
     * @return a CompletableFuture that completes when the task finishes
     */
    public CompletableFuture<Void> runRegion(FPlayer fPlayer, SchedulerRunnable runnable) {
        return runAsync(runnable);
    }

    /**
     * Schedules a task to run asynchronously on a repeating interval for all players,
     * with a default initial delay of 5 ticks. The consumer receives each platform player.
     *
     * @param fPlayerConsumer the consumer to apply to each platform player
     * @param period the number of ticks between consecutive executions
     * @return a CompletableFuture that completes when the task is first executed
     */
    public CompletableFuture<Void> runPlayerAsyncTimer(Consumer<FPlayer> fPlayerConsumer, long period) {
        return runPlayerAsyncTimer(fPlayerConsumer, 5L, period);
    }

    /**
     * Schedules a task to run asynchronously on a repeating interval for all players
     * with custom delay and period. The consumer receives each platform player.
     *
     * @param fPlayerConsumer the consumer to apply to each platform player
     * @param delay the number of ticks to wait before the first execution
     * @param period the number of ticks between consecutive executions
     * @return a CompletableFuture that completes when the task is first executed
     */
    public CompletableFuture<Void> runPlayerAsyncTimer(Consumer<FPlayer> fPlayerConsumer, long delay, long period) {
        return runAsyncTimer(() -> fPlayerServiceProvider.get().getPlatformFPlayers().forEach(fPlayerConsumer), delay, period);
    }

    /**
     * Executes a task immediately on the current thread without scheduling.
     *
     * @param runnable the task to execute immediately
     * @return a CompletableFuture that completes when the task finishes
     */
    public CompletableFuture<Void> runImmediately(SchedulerRunnable runnable) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        wrapExceptionRunnable(runnable, completableFuture).run();

        return completableFuture;
    }

    /**
     * Checks whether the scheduler is currently disabled.
     *
     * @return true if the scheduler is disabled, false otherwise
     */
    public boolean isDisabled() {
        return disabled || FlectonePulseAPI.isDisabling();
    }

    /**
     * Advances the scheduler by one tick and processes all tasks scheduled for the current tick.
     * This method should be called regularly by the game loop or timing mechanism.
     */
    public void onTick() {
        processTasks(currentTick.getAndIncrement());
    }

    private boolean isAsyncThread() {
        return Thread.currentThread().getName().startsWith(THREAD_PREFIX);
    }

    private void processTasks(long tick) {
        List<ScheduledTask> tasks = scheduledTasks.remove(tick);
        if (tasks == null) return;

        List<ScheduledTask> syncTasks = new ArrayList<>();

        for (ScheduledTask scheduledTask : tasks) {
            if (scheduledTask.async()) {
                execute(scheduledTask);
            } else {
                syncTasks.add(scheduledTask);
            }
        }

        syncTasks.forEach(this::execute);
    }

    private void execute(ScheduledTask scheduledTask) {
        if (scheduledTask.future().isCancelled()) return;

        Runnable runnable = wrapExceptionRunnable(scheduledTask);
        if (scheduledTask.async()) {
            execute(runnable);
        } else {
            runnable.run();
        }

        if (scheduledTask.isRepeating() && !scheduledTask.future().isCancelled()) {
            rescheduleTask(scheduledTask);
        }
    }

    private void execute(Runnable runnable) {
        try {
            executorService.execute(runnable);
        } catch (RejectedExecutionException _) {
            fLogger.warning("Executor overloaded, increase 'max_pool_size' or switch 'work_queue' to 'LINKED_BLOCKING' in config.yml. Running in current thread...");
            runnable.run();
        }
    }

    private Runnable wrapExceptionRunnable(ScheduledTask scheduledTask) {
        return wrapExceptionRunnable(scheduledTask.runnable(), scheduledTask.future());
    }

    protected Runnable wrapExceptionRunnable(SchedulerRunnable runnable, CompletableFuture<Void> future) {
        return () -> {
            try {
                runnable.run();
                if (!future.isCancelled()) {
                    future.complete(null);
                }
            } catch (Exception e) {
                fLogger.warning(e, "Task execution failed:");
                future.completeExceptionally(e);
            }
        };
    }

    private void rescheduleTask(ScheduledTask task) {
        registerTask(task.withNextTick(task.nextTick() + task.period()));
    }

    private ScheduledTask registerTask(SchedulerRunnable runnable, long nextTick, long period, boolean async) {
        return registerTask(new ScheduledTask(runnable, nextTick, period, async, new CompletableFuture<>()));
    }

    private ScheduledTask registerTask(ScheduledTask task) {
        scheduledTasks.compute(task.nextTick(), (_, tasks) -> {
            List<ScheduledTask> list = (tasks != null) ? tasks : new CopyOnWriteArrayList<>();
            list.add(task);
            return list;
        });

        return task;
    }

    private ExecutorService createExecutorService() {
        config = fileFacadeProvider.get().config().executor();

        ThreadFactory factory = Thread.ofPlatform()
                .name(THREAD_PREFIX, threadCounter.getAndIncrement())
                .factory();

        return new ThreadPoolExecutor(
                config.minPoolSize(),
                config.maxPoolSize() == -1 ? Integer.MAX_VALUE : config.maxPoolSize(),
                config.keepAlive().duration(), config.keepAlive().timeUnit(),
                config.workQueue() == Config.Executor.WorkQueue.SYNCHRONOUS ? new SynchronousQueue<>() : new LinkedBlockingQueue<>(),
                factory
        );
    }

    @With
    private record ScheduledTask(
            SchedulerRunnable runnable,
            long nextTick,
            long period,
            boolean async,
            CompletableFuture<Void> future
    ) {

        boolean isRepeating() {
            return period > 0;
        }

    }
}