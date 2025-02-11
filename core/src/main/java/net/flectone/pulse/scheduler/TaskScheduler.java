package net.flectone.pulse.scheduler;

import com.google.inject.Inject;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.model.FPlayer;

import java.util.function.Consumer;

public abstract class TaskScheduler {

    @Inject private FPlayerManager fPlayerManager;

    public TaskScheduler() {}

    public abstract void runAsync(RunnableException runnable);

    public abstract void runAsyncTimer(RunnableException runnable, long tick, long period);

    public abstract void runAsyncLater(RunnableException runnable, long tick);

    public abstract void runSync(RunnableException runnable);

    public abstract void runSyncLater(RunnableException runnable, long tick);

    public abstract void reload();

    public void runAsyncTicker(Consumer<FPlayer> fPlayerConsumer, long tick) {
        runAsyncTimer(() -> fPlayerManager.getFPlayers().forEach(fPlayerConsumer), tick, tick);
    }
}
