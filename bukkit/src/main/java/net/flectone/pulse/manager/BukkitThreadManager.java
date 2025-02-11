package net.flectone.pulse.manager;

import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.plugin.Plugin;

@Singleton
public class BukkitThreadManager extends ThreadManager {

    private final Plugin plugin;
    private final TaskScheduler taskScheduler;

    @Inject
    public BukkitThreadManager(Plugin plugin,
                               TaskScheduler taskScheduler) {
        this.plugin = plugin;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void runAsync(Runnable runnable) {
        taskScheduler.runTaskAsynchronously(runnable);
    }

    @Override
    public void runAsyncTimer(Runnable runnable, long tick, long period) {
        taskScheduler.runTaskTimerAsynchronously(runnable, tick, period);
    }

    @Override
    public void runAsyncLater(Runnable runnable, long tick) {
        taskScheduler.runTaskLaterAsynchronously(runnable, tick);
    }

    @Override
    public void runSync(Runnable runnable) {
        taskScheduler.runTask(runnable);
    }

    public void runSyncLater(Runnable runnable, long tick) {
        taskScheduler.runTaskLater(runnable, tick);
    }

    @Override
    public void reload() {
        taskScheduler.cancelTasks(plugin);
    }
}
