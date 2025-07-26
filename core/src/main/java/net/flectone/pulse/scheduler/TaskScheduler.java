package net.flectone.pulse.scheduler;

public interface TaskScheduler {

    void runAsync(SchedulerRunnable runnable);

    void runAsyncTimer(SchedulerRunnable runnable, long tick, long period);

    void runAsyncTimer(SchedulerRunnable runnable, long tick);

    void runAsyncLater(SchedulerRunnable runnable, long tick);

    void runSync(SchedulerRunnable runnable);

    void runSyncRegion(Object entity, SchedulerRunnable runnable);

    void runSyncTimer(SchedulerRunnable runnable, long tick, long period);

    void runSyncTimer(SchedulerRunnable runnable, long tick);

    void runSyncLater(SchedulerRunnable runnable, long tick);

    void reload();

}
