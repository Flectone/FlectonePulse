package net.flectone.pulse.scheduler;

public interface TaskScheduler {

    void runAsync(RunnableException runnable);

    void runAsyncTimer(RunnableException runnable, long tick, long period);

    void runAsyncTimer(RunnableException runnable, long tick);

    void runAsyncLater(RunnableException runnable, long tick);

    void runSync(RunnableException runnable);

    void runSyncTimer(RunnableException runnable, long tick, long period);

    void runSyncTimer(RunnableException runnable, long tick);

    void runSyncLater(RunnableException runnable, long tick);

    void reload();

}
