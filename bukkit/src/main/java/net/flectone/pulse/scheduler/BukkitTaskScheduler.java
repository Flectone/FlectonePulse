package net.flectone.pulse.scheduler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Setter;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.entity.Entity;
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
    public void runAsync(SchedulerRunnable runnable) {
        if (disabled) {
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
    public void runAsyncTimer(SchedulerRunnable runnable, long tick, long period) {
        if (disabled) {
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
    public void runAsyncTimer(SchedulerRunnable runnable, long tick) {
        runAsyncTimer(runnable, tick, tick);
    }

    @Override
    public void runAsyncLater(SchedulerRunnable runnable, long tick) {
        if (disabled) {
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
    public void runSync(SchedulerRunnable runnable) {
        if (disabled) {
            return;
        }

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
    public void runSyncRegion(Object entity, SchedulerRunnable runnable) {
        if (disabled) {
            return;
        }

        if (!(entity instanceof Entity bukkitEntity)) {
            runSync(runnable);
            return;
        }

        taskScheduler.runTask(bukkitEntity.getLocation(), () -> {
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
    public void runSyncTimer(SchedulerRunnable runnable, long tick, long period) {
        if (disabled) {
            return;
        }

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
    public void runSyncTimer(SchedulerRunnable runnable, long tick) {
        runSyncTimer(runnable, tick, tick);
    }

    @Override
    public void runSyncLater(SchedulerRunnable runnable, long tick) {
        if (disabled) {
            return;
        }

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
