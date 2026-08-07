package net.flectone.pulse.scheduler;

import net.flectone.pulse.model.entity.FPlayer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
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
public interface TaskScheduler {

    /**
     * The pool the async tasks run on.
     *
     * @return the executor
     */
    ExecutorService getExecutorService();

    /**
     * Runs the pending tasks, clears the queue and resets the tick counter.
     */
    void reload();

    /**
     * Starts the scheduler by creating and initializing the executor service
     * with the configured thread pool settings.
     */
    void start();

    /**
     * Shuts down the scheduler gracefully by disabling new task submissions,
     * processing remaining tasks at the current tick, and waiting for
     * executing tasks to complete within the configured timeout period.
     * If tasks don't complete within the timeout, forces immediate shutdown.
     */
    void shutdown();

    /**
     * Schedules a task to run asynchronously on the thread pool.
     *
     * @param runnable the task to execute
     * @return a CompletableFuture that completes when the task finishes
     */
    CompletableFuture<Void> runAsync(SchedulerRunnable runnable);

    /**
     * Schedules a task to run asynchronously with control over independent execution.
     * If not independent and already on an async thread, runs immediately to avoid unnecessary threading.
     *
     * @param runnable the task to execute
     * @param independent if true, always schedules even if already on async thread; if false, may run immediately
     * @return a CompletableFuture that completes when the task finishes
     */
    CompletableFuture<Void> runAsync(SchedulerRunnable runnable, boolean independent);

    /**
     * Schedules a task to run asynchronously after a default delay of 20 ticks.
     *
     * @param runnable the task to execute
     * @return a CompletableFuture that completes when the task finishes
     */
    CompletableFuture<Void> runAsyncLater(SchedulerRunnable runnable);

    /**
     * Schedules a task to run asynchronously after the specified delay.
     *
     * @param runnable the task to execute
     * @param delay the number of ticks to wait before executing the task
     * @return a CompletableFuture that completes when the task finishes
     */
    CompletableFuture<Void> runAsyncLater(SchedulerRunnable runnable, long delay);

    /**
     * Schedules a task to run asynchronously on a repeating interval with a default initial delay of 5 ticks.
     *
     * @param runnable the task to execute repeatedly
     * @param period the number of ticks between consecutive executions
     * @return a CompletableFuture that completes when the task is first executed
     */
    CompletableFuture<Void> runAsyncTimer(SchedulerRunnable runnable, long period);

    /**
     * Schedules a task to run asynchronously on a repeating interval with custom delay and period.
     *
     * @param runnable the task to execute repeatedly
     * @param delay the number of ticks to wait before the first execution
     * @param period the number of ticks between consecutive executions
     * @return a CompletableFuture that completes when the task is first executed
     */
    CompletableFuture<Void> runAsyncTimer(SchedulerRunnable runnable, long delay, long period);

    /**
     * Schedules a task to run synchronously on the current thread.
     *
     * @param runnable the task to execute
     * @return a CompletableFuture that completes when the task finishes
     */
    CompletableFuture<Void> runSync(SchedulerRunnable runnable);

    /**
     * Schedules a task to run synchronously on the current thread after a specified delay.
     *
     * @param runnable the task to execute
     * @param delay the number of ticks to wait before executing the task
     * @return a CompletableFuture that completes when the task finishes
     */
    CompletableFuture<Void> runSyncLater(SchedulerRunnable runnable, long delay);

    /**
     * Schedules a task to run synchronously on a repeating interval with a default initial delay of 5 ticks.
     *
     * @param runnable the task to execute repeatedly
     * @param period the number of ticks between consecutive executions
     * @return a CompletableFuture that completes when the task is first executed
     */
    CompletableFuture<Void> runSyncTimer(SchedulerRunnable runnable, long period);

    /**
     * Schedules a task to run synchronously on a repeating interval with custom delay and period.
     *
     * @param runnable the task to execute repeatedly
     * @param delay the number of ticks to wait before the first execution
     * @param period the number of ticks between consecutive executions
     * @return a CompletableFuture that completes when the task is first executed
     */
    CompletableFuture<Void> runSyncTimer(SchedulerRunnable runnable, long delay, long period);

    /**
     * Schedules a region-related task to run asynchronously.
     *
     * @param fPlayer the player associated with the region (currently unused)
     * @param runnable the task to execute
     * @return a CompletableFuture that completes when the task finishes
     */
    CompletableFuture<Void> runRegion(FPlayer fPlayer, SchedulerRunnable runnable);

    /**
     * Schedules a task to run asynchronously on a repeating interval for all players,
     * with a default initial delay of 5 ticks. The consumer receives each platform player.
     *
     * @param fPlayerConsumer the consumer to apply to each platform player
     * @param period the number of ticks between consecutive executions
     * @return a CompletableFuture that completes when the task is first executed
     */
    CompletableFuture<Void> runPlayerAsyncTimer(Consumer<FPlayer> fPlayerConsumer, long period);

    /**
     * Schedules a task to run asynchronously on a repeating interval for all players
     * with custom delay and period. The consumer receives each platform player.
     *
     * @param fPlayerConsumer the consumer to apply to each platform player
     * @param delay the number of ticks to wait before the first execution
     * @param period the number of ticks between consecutive executions
     * @return a CompletableFuture that completes when the task is first executed
     */
    CompletableFuture<Void> runPlayerAsyncTimer(Consumer<FPlayer> fPlayerConsumer, long delay, long period);

    /**
     * Executes a task immediately on the current thread without scheduling.
     *
     * @param runnable the task to execute immediately
     * @return a CompletableFuture that completes when the task finishes
     */
    CompletableFuture<Void> runImmediately(SchedulerRunnable runnable);

    /**
     * Checks whether the scheduler is currently disabled.
     *
     * @return true if the scheduler is disabled, false otherwise
     */
    boolean isDisabled();

    /**
     * Advances the scheduler by one tick and processes all tasks scheduled for the current tick.
     * This method should be called regularly by the game loop or timing mechanism.
     */
    void onTick();

}