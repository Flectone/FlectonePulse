package net.flectone.pulse.scheduler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Setter;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;

@Singleton
public class BukkitTaskScheduler implements TaskScheduler {

    private final Plugin plugin;
    private final com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler taskScheduler;
    private final FLogger fLogger;

    @Setter private boolean disabled = false;

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
        if (disabled) {
            runSync(runnable);
            return;
        }

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
        if (disabled) {
            runSyncTimer(runnable, tick, period);
            return;
        }

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
        if (disabled) {
            runSyncLater(runnable, tick);
            return;
        }

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
    public void runSyncTimer(RunnableException runnable, long tick, long period) {
        taskScheduler.runTaskTimer(() -> {
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
    public void runSyncTimer(RunnableException runnable, long tick) {
        runSyncTimer(runnable, tick, tick);
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
