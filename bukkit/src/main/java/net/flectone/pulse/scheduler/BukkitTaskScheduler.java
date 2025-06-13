package net.flectone.pulse.scheduler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Setter;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.plugin.Plugin;

@Singleton
public class BukkitTaskScheduler implements TaskScheduler {

    private final Plugin plugin;
    private final com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler taskScheduler;
    private final FLogger fLogger;

    @Setter
    private boolean enable = true;

    @Inject
    public BukkitTaskScheduler(Plugin plugin,
                               com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler taskScheduler,
                               FLogger fLogger) {
        this.plugin = plugin;
        this.taskScheduler = taskScheduler;
        this.fLogger = fLogger;
    }

    @Override
    public void runAsync(RunnableException runnable) {
        if (!enable) return;

        taskScheduler.runTaskAsynchronously(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                fLogger.warning(e);
            }
        });
    }

    @Override
    public void runAsyncTimer(RunnableException runnable, long tick, long period) {
        if (!enable) return;

        taskScheduler.runTaskTimerAsynchronously(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                fLogger.warning(e);
            }
        }, tick, period);
    }

    @Override
    public void runAsyncTimer(RunnableException runnable, long tick) {
        if (!enable) return;

        runAsyncTimer(runnable, tick, tick);
    }

    @Override
    public void runAsyncLater(RunnableException runnable, long tick) {
        if (!enable) return;

        taskScheduler.runTaskLaterAsynchronously(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                fLogger.warning(e);
            }
        }, tick);
    }

    @Override
    public void runSync(RunnableException runnable) {
        if (!enable) return;

        taskScheduler.runTask(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                fLogger.warning(e);
            }
        });
    }

    @Override
    public void runSyncLater(RunnableException runnable, long tick) {
        if (!enable) return;

        taskScheduler.runTaskLater(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                fLogger.warning(e);
            }
        }, tick);
    }

    @Override
    public void reload() {
        taskScheduler.cancelTasks(plugin);
    }
}
