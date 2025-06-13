package net.flectone.pulse.scheduler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;

@Singleton
public class BukkitTaskScheduler implements TaskScheduler {

    private final Plugin plugin;
    private final com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler taskScheduler;
    private final FLogger fLogger;

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
        taskScheduler.runTaskAsynchronously(() -> {
            try {
                runnable.run();
            } catch (IllegalPluginAccessException ignored) {
                // ignore this
            } catch (Exception e) {
                fLogger.warning(e);
            }
        });
    }

    @Override
    public void runAsyncTimer(RunnableException runnable, long tick, long period) {
        taskScheduler.runTaskTimerAsynchronously(() -> {
            try {
                runnable.run();
            } catch (IllegalPluginAccessException ignored) {
                // ignore this
            } catch (Exception e) {
                fLogger.warning(e);
            }
        }, tick, period);
    }

    @Override
    public void runAsyncTimer(RunnableException runnable, long tick) {
        runAsyncTimer(runnable, tick, tick);
    }

    @Override
    public void runAsyncLater(RunnableException runnable, long tick) {
        taskScheduler.runTaskLaterAsynchronously(() -> {
            try {
                runnable.run();
            } catch (IllegalPluginAccessException ignored) {
                // ignore this
            } catch (Exception e) {
                fLogger.warning(e);
            }
        }, tick);
    }

    @Override
    public void runSync(RunnableException runnable) {
        taskScheduler.runTask(() -> {
            try {
                runnable.run();
            } catch (IllegalPluginAccessException ignored) {
                // ignore this
            } catch (Exception e) {
                fLogger.warning(e);
            }
        });
    }

    @Override
    public void runSyncLater(RunnableException runnable, long tick) {
        taskScheduler.runTaskLater(() -> {
            try {
                runnable.run();
            } catch (IllegalPluginAccessException ignored) {
                // ignore this
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
