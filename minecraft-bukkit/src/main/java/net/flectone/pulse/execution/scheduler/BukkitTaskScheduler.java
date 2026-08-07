package net.flectone.pulse.execution.scheduler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.LazyInstance;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.entity.Entity;

import java.util.concurrent.CompletableFuture;

@Singleton
public class BukkitTaskScheduler extends TaskSchedulerImpl {

    private final com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler taskScheduler;
    private final LazyInstance<FPlayerService> fPlayerService;
    private final LazyInstance<PlatformPlayerAdapter> platformPlayerAdapter;
    private final LazyInstance<PlatformServerAdapter> platformServerAdapter;
    private final ReflectionResolver reflectionResolver;

    @Inject
    public BukkitTaskScheduler(FLogger logger,
                               com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler taskScheduler,
                               LazyInstance<FPlayerService> fPlayerService,
                               LazyInstance<PlatformPlayerAdapter> platformPlayerAdapter,
                               LazyInstance<PlatformServerAdapter> platformServerAdapter,
                               LazyInstance<FileFacade> fileFacade,
                               ReflectionResolver reflectionResolver) {
        super(logger, fPlayerService, fileFacade);

        this.taskScheduler = taskScheduler;
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.platformServerAdapter = platformServerAdapter;
        this.reflectionResolver = reflectionResolver;
    }

    @Override
    public CompletableFuture<Void> runRegion(FPlayer fPlayer, SchedulerRunnable runnable) {
        if (isDisabled()) return runImmediately(runnable);

        if (!reflectionResolver.isFolia()) {
            return runAsync(runnable);
        }

        if (platformServerAdapter.get().isPrimaryThread()) {
            return runImmediately(runnable);
        }

        Object entity = platformPlayerAdapter.get().convertToPlatformPlayer(convertUnknownFPlayer(fPlayer));
        if (!(entity instanceof Entity bukkitEntity)) {
            return runAsync(runnable);
        }

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        taskScheduler.runTask(bukkitEntity, () -> wrapExceptionRunnable(runnable, completableFuture).run());

        return completableFuture;
    }

    private FPlayer convertUnknownFPlayer(FPlayer fPlayer) {
        return fPlayer.isUnknown() || fPlayer.isConsole() ? fPlayerService.get().getRandomFPlayer() : fPlayer;
    }

}