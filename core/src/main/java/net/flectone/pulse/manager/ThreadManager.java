package net.flectone.pulse.manager;

public abstract class ThreadManager {

    public ThreadManager() {
    }

    public abstract void runAsync(Runnable runnable);
    public abstract void runAsyncTimer(Runnable runnable, long tick, long period);
    public abstract void runAsyncLater(Runnable runnable, long tick);
    public abstract void runSync(Runnable runnable);
    public abstract void runSyncLater(Runnable runnable, long tick);
    public abstract void reload();
}
