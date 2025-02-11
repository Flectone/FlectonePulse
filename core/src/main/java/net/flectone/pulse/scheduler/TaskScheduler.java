package net.flectone.pulse.scheduler;

public abstract class TaskScheduler {

    public TaskScheduler() {
    }

    public abstract void runAsync(RunnableException runnable);
    public abstract void runAsyncTimer(RunnableException runnable, long tick, long period);
    public abstract void runAsyncLater(RunnableException runnable, long tick);
    public abstract void runSync(RunnableException runnable);
    public abstract void runSyncLater(RunnableException runnable, long tick);
    public abstract void reload();
}
