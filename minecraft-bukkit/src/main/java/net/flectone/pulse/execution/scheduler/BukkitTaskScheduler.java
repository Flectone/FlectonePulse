package net.flectone.pulse.execution.scheduler;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.entity.Entity;

import java.util.concurrent.CompletableFuture;

@Singleton
public class BukkitTaskScheduler extends TaskScheduler {

    private final com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler taskScheduler;
    private final Provider<FPlayerService> fPlayerServiceProvider;
    private final Provider<PlatformPlayerAdapter> platformPlayerAdapterProvider;
    private final Provider<PlatformServerAdapter> platformServerAdapterProvider;
    private final ReflectionResolver reflectionResolver;

    @Inject
    public BukkitTaskScheduler(FLogger logger,
                               com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler taskScheduler,
                               Provider<FPlayerService> fPlayerServiceProvider,
                               Provider<PlatformPlayerAdapter> platformPlayerAdapterProvider,
                               Provider<PlatformServerAdapter> platformServerAdapterProvider,
                               ReflectionResolver reflectionResolver) {
        super(logger, fPlayerServiceProvider);

        this.taskScheduler = taskScheduler;
        this.fPlayerServiceProvider = fPlayerServiceProvider;
        this.platformPlayerAdapterProvider = platformPlayerAdapterProvider;
        this.platformServerAdapterProvider = platformServerAdapterProvider;
        this.reflectionResolver = reflectionResolver;
    }

    @Override
    public CompletableFuture<Void> runRegion(FPlayer fPlayer, SchedulerRunnable runnable) {
        if (isDisabled()) return runImmediately(runnable);

        if (!reflectionResolver.isFolia()) {
            return runAsync(runnable);
        }

        if (platformServerAdapterProvider.get().isPrimaryThread()) {
            return runImmediately(runnable);
        }

        Object entity = platformPlayerAdapterProvider.get().convertToPlatformPlayer(convertUnknownFPlayer(fPlayer));
        if (!(entity instanceof Entity bukkitEntity)) {
            return runAsync(runnable);
        }

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        taskScheduler.runTask(bukkitEntity, () -> wrapExceptionRunnable(runnable, completableFuture).run());

        return completableFuture;
    }

    private FPlayer convertUnknownFPlayer(FPlayer fPlayer) {
        return fPlayer.isUnknown() || fPlayer.isConsole() ? fPlayerServiceProvider.get().getRandomFPlayer() : fPlayer;
    }

}
