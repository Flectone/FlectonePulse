package net.flectone.pulse.execution.scheduler;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.exception.SchedulerTaskException;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitTaskScheduler implements TaskScheduler {

    private final Plugin plugin;
    private final com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler taskScheduler;
    private final FLogger fLogger;
    private final Provider<FPlayerService> fPlayerServiceProvider;
    private final Provider<PlatformPlayerAdapter> platformPlayerAdapterProvider;
    private final Provider<PlatformServerAdapter> platformServerAdapterProvider;
    private final ReflectionResolver reflectionResolver;

    private volatile boolean disabled = false;

    @Override
    public void shutdown() {
        disabled = true;

        taskScheduler.cancelTasks(plugin);
    }

    @Override
    public void start() {
        disabled = false;
    }

    @Override
    public void runAsync(SchedulerRunnable runnable, boolean independent) {
        if (runDisabledTask(runnable)) return;

        if (!independent && isAsyncThread()) {
            wrapExceptionRunnable(runnable).run();
            return;
        }

        taskScheduler.runTaskAsynchronously(() -> wrapExceptionRunnable(runnable).run());
    }

    @Override
    public void runAsyncLater(SchedulerRunnable runnable, long delay) {
        if (runDisabledTask(runnable)) return;

        taskScheduler.runTaskLaterAsynchronously(() -> wrapExceptionRunnable(runnable).run(), delay);
    }

    @Override
    public void runAsyncTimer(SchedulerRunnable runnable, long delay, long period) {
        if (runDisabledTask(runnable)) return;

        taskScheduler.runTaskTimerAsynchronously(() -> wrapExceptionRunnable(runnable).run(), delay, period);
    }

    @Override
    public void runSync(SchedulerRunnable runnable) {
        if (runDisabledTask(runnable)) return;

        if (!isAsyncThread() && !reflectionResolver.isFolia()) {
            wrapExceptionRunnable(runnable).run();
            return;
        }

        taskScheduler.runTask(() -> wrapExceptionRunnable(runnable).run());
    }

    @Override
    public void runSyncLater(SchedulerRunnable runnable, long delay) {
        if (runDisabledTask(runnable)) return;

        taskScheduler.runTaskLater(() -> wrapExceptionRunnable(runnable).run(), delay);
    }

    @Override
    public void runSyncTimer(SchedulerRunnable runnable, long delay, long period) {
        if (runDisabledTask(runnable)) return;

        taskScheduler.runTaskTimer(() -> wrapExceptionRunnable(runnable).run(), delay, period);
    }

    @Override
    public void runRegion(FPlayer fPlayer, SchedulerRunnable runnable) {
        if (runDisabledTask(runnable)) return;
        if (!reflectionResolver.isFolia()) {
            runAsync(runnable);
            return;
        }

        if (platformServerAdapterProvider.get().isPrimaryThread()) {
            try {
                runnable.run();
                return;
            } catch (Exception _) {
                // ignore exception
            }
        }

        Object entity = platformPlayerAdapterProvider.get().convertToPlatformPlayer(convertUnknownFPlayer(fPlayer));
        if (!(entity instanceof Entity bukkitEntity)) {
            runAsync(runnable);
            return;
        }

        taskScheduler.runTask(bukkitEntity, () -> wrapExceptionRunnable(runnable).run());
    }

    @Override
    public void runPlayerAsyncTimer(Consumer<FPlayer> fPlayerConsumer, long delay, long period) {
        runAsyncTimer(() -> fPlayerServiceProvider.get().getPlatformFPlayers().forEach(fPlayerConsumer), delay, period);
    }

    @Override
    public Runnable wrapExceptionRunnable(SchedulerRunnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (IllegalPluginAccessException _) {
                // ignore shit exception
            } catch (SchedulerTaskException e) {
                fLogger.warning(e);
            }
        };
    }

    @Override
    public boolean isDisabled() {
        return disabled || TaskScheduler.super.isDisabled();
    }

    private FPlayer convertUnknownFPlayer(FPlayer fPlayer) {
        return fPlayer.isUnknown() || fPlayer.isConsole() ? fPlayerServiceProvider.get().getRandomFPlayer() : fPlayer;
    }

    private boolean isAsyncThread() {
        return !platformServerAdapterProvider.get().isPrimaryThread() && !isRestrictedAsyncThread();
    }

}
