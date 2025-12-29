package net.flectone.pulse.execution.scheduler;

import net.flectone.pulse.model.entity.FPlayer;

import java.util.function.Consumer;

/**
 * Task scheduler for managing asynchronous and synchronous execution in FlectonePulse.
 * Provides methods for scheduling tasks with various timing and threading options.
 *
 * @author TheFaser
 * @since 0.8.0
 */
public interface TaskScheduler {

    /**
     * Shuts down the scheduler and cancels all pending tasks.
     */
    void shutdown();

    /**
     * Reloads the scheduler configuration.
     */
    void reload();

    /**
     * Runs a task asynchronously.
     *
     * @param runnable the task to run
     * @param independent whether the task should run in an independent thread
     */
    void runAsync(SchedulerRunnable runnable, boolean independent);

    /**
     * Runs a task asynchronously after a delay.
     *
     * @param runnable the task to run
     * @param tick the delay in ticks (1 tick = 50ms)
     */
    void runAsyncLater(SchedulerRunnable runnable, long tick);

    /**
     * Runs a repeating task asynchronously.
     *
     * @param runnable the task to run
     * @param tick the initial delay in ticks
     * @param period the period between executions in ticks
     */
    void runAsyncTimer(SchedulerRunnable runnable, long tick, long period);

    /**
     * Runs a task synchronously on the main server thread.
     *
     * @param runnable the task to run
     */
    void runSync(SchedulerRunnable runnable);

    /**
     * Runs a task synchronously after a delay.
     *
     * @param runnable the task to run
     * @param tick the delay in ticks
     */
    void runSyncLater(SchedulerRunnable runnable, long tick);

    /**
     * Runs a repeating task synchronously.
     *
     * @param runnable the task to run
     * @param tick the initial delay in ticks
     * @param period the period between executions in ticks
     */
    void runSyncTimer(SchedulerRunnable runnable, long tick, long period);

    /**
     * Runs a task in the player's region.
     * On Folia: executes in the player's region thread.
     * Otherwise: executes asynchronously.
     *
     * @param fPlayer the player whose region to use
     * @param runnable the task to run
     */
    void runRegion(FPlayer fPlayer, SchedulerRunnable runnable);

    /**
     * Runs a task in the player's region after a delay.
     * On Folia: executes in the player's region thread.
     * Otherwise: executes asynchronously.
     *
     * @param fPlayer the player whose region to use
     * @param runnable the task to run
     * @param tick the delay in ticks
     */
    void runRegionLater(FPlayer fPlayer, SchedulerRunnable runnable, long tick);

    /**
     * Runs a repeating task in the player's region.
     * On Folia: executes in the player's region thread.
     * Otherwise: executes asynchronously.
     *
     * @param fPlayer the player whose region to use
     * @param runnable the task to run
     * @param tick the initial delay in ticks
     * @param period the period between executions in ticks
     */
    void runRegionTimer(FPlayer fPlayer, SchedulerRunnable runnable, long tick, long period);

    /**
     * Runs a repeating task for all players in their respective regions.
     * On Folia: executes in each player's region thread.
     * Otherwise: executes asynchronously.
     *
     * @param fPlayerConsumer the consumer to apply to each player
     * @param tick the period between executions in ticks
     */
    void runPlayerRegionTimer(Consumer<FPlayer> fPlayerConsumer, long tick);

    /**
     * Wraps a runnable with exception handling.
     *
     * @param runnable the runnable to wrap
     * @return wrapped runnable with exception handling
     */
    Runnable wrapExceptionRunnable(SchedulerRunnable runnable);

    /**
     * Runs a task asynchronously with default independent flag (false).
     *
     * @param runnable the task to run
     */
    default void runAsync(SchedulerRunnable runnable) {
        runAsync(runnable, false);
    }

    /**
     * Runs a task asynchronously after default delay (20 ticks = 1 second).
     *
     * @param runnable the task to run
     */
    default void runAsyncLater(SchedulerRunnable runnable) {
        runAsyncLater(runnable, 20L);
    }

    /**
     * Runs a repeating task asynchronously with same initial delay and period.
     *
     * @param runnable the task to run
     * @param tick both initial delay and period in ticks
     */
    default void runAsyncTimer(SchedulerRunnable runnable, long tick) {
        runAsyncTimer(runnable, tick, tick);
    }

    /**
     * Runs a repeating task synchronously with same initial delay and period.
     *
     * @param runnable the task to run
     * @param tick both initial delay and period in ticks
     */
    default void runSyncTimer(SchedulerRunnable runnable, long tick) {
        runSyncTimer(runnable, tick, tick);
    }

    /**
     * Runs a repeating task in the player's region with same initial delay and period.
     *
     * @param fPlayer the player whose region to use
     * @param runnable the task to run
     * @param tick both initial delay and period in ticks
     */
    default void runRegionTimer(FPlayer fPlayer, SchedulerRunnable runnable, long tick) {
        runRegionTimer(fPlayer, runnable, tick, tick);
    }

    /**
     * Checks if the current thread is restricted for async operations.
     *
     * @return true if thread is restricted (Netty or Async Chat thread)
     */
    default boolean isRestrictedAsyncThread() {
        String threadName = Thread.currentThread().getName();

        return threadName.startsWith("Netty") || threadName.startsWith("Async Chat");
    }

}
