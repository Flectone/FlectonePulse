package net.flectone.pulse.execution.scheduler;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.With;
import net.flectone.pulse.FlectonePulseAPI;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Singleton
public class TaskSchedulerImpl implements TaskScheduler {

    private static final String THREAD_PREFIX = "FlectonePulse Thread #";

    private final AtomicLong currentTick = new AtomicLong(0L);
    private final AtomicLong threadCounter = new AtomicLong(0L);
    private final Map<Long, List<ScheduledTask>> scheduledTasks = new ConcurrentSkipListMap<>();
    private final FLogger fLogger;
    private final Provider<FPlayerService> fPlayerServiceProvider;

    @Inject
    private Provider<FileFacade> fileFacadeProvider;

    @Getter
    private ExecutorService executorService;
    private Config.Executor config;

    private volatile boolean disabled = false;

    @Inject
    public TaskSchedulerImpl(FLogger fLogger,
                         Provider<FPlayerService> fPlayerServiceProvider) {
        this.fLogger = fLogger;
        this.fPlayerServiceProvider = fPlayerServiceProvider;
    }

    @Override
    public void reload() {
        processTasks(currentTick.get());
        scheduledTasks.clear();
        currentTick.set(0L);
    }

    @Override
    public void start() {
        executorService = createExecutorService();
    }

    @Override
    public void shutdown() {
        disabled = true;

        processTasks(currentTick.get());

        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(config.shutdownTimeout().duration(), config.shutdownTimeout().timeUnit())) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            fLogger.warning(e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            scheduledTasks.clear();
        }
    }

    @Override
    public CompletableFuture<Void> runAsync(SchedulerRunnable runnable) {
        return runAsync(runnable, false);
    }

    @Override
    public CompletableFuture<Void> runAsync(SchedulerRunnable runnable, boolean independent) {
        if (isDisabled()) return runImmediately(runnable);

        if (!independent && isAsyncThread()) {
            return runImmediately(runnable);
        }

        // we don't need to create a task to do this in async
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        execute(wrapExceptionRunnable(runnable, completableFuture));

        return completableFuture;
    }

    @Override
    public CompletableFuture<Void> runAsyncLater(SchedulerRunnable runnable) {
        return runAsyncLater(runnable, 20L);
    }

    @Override
    public CompletableFuture<Void> runAsyncLater(SchedulerRunnable runnable, long delay) {
        if (isDisabled()) return runImmediately(runnable);

        long firstTick = currentTick.get() + delay;
        return registerTask(runnable, firstTick, -1L, true).future();
    }

    @Override
    public CompletableFuture<Void> runAsyncTimer(SchedulerRunnable runnable, long period) {
        return runAsyncTimer(runnable, 5L, period);
    }

    @Override
    public CompletableFuture<Void> runAsyncTimer(SchedulerRunnable runnable, long delay, long period) {
        if (isDisabled()) return runImmediately(runnable);

        long firstTick = currentTick.get() + delay;
        return registerTask(runnable, firstTick, period, true).future();
    }

    @Override
    public CompletableFuture<Void> runSync(SchedulerRunnable runnable) {
        if (isDisabled()) return runImmediately(runnable);

        return registerTask(runnable, currentTick.get(), -1L, false).future();
    }

    @Override
    public CompletableFuture<Void> runSyncLater(SchedulerRunnable runnable, long delay) {
        if (isDisabled()) return runImmediately(runnable);

        long firstTick = currentTick.get() + delay;
        return registerTask(runnable, firstTick, -1L, false).future();
    }

    @Override
    public CompletableFuture<Void> runSyncTimer(SchedulerRunnable runnable, long period) {
        return runSyncTimer(runnable, 5L, period);
    }

    @Override
    public CompletableFuture<Void> runSyncTimer(SchedulerRunnable runnable, long delay, long period) {
        if (isDisabled()) return runImmediately(runnable);

        long firstTick = currentTick.get() + delay;
        return registerTask(runnable, firstTick, period, false).future();
    }

    @Override
    public CompletableFuture<Void> runRegion(FPlayer fPlayer, SchedulerRunnable runnable) {
        return runAsync(runnable);
    }

    @Override
    public CompletableFuture<Void> runPlayerAsyncTimer(Consumer<FPlayer> fPlayerConsumer, long period) {
        return runPlayerAsyncTimer(fPlayerConsumer, 5L, period);
    }

    @Override
    public CompletableFuture<Void> runPlayerAsyncTimer(Consumer<FPlayer> fPlayerConsumer, long delay, long period) {
        return runAsyncTimer(() -> fPlayerServiceProvider.get().getPlatformFPlayers().forEach(fPlayerConsumer), delay, period);
    }

    @Override
    public CompletableFuture<Void> runImmediately(SchedulerRunnable runnable) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        wrapExceptionRunnable(runnable, completableFuture).run();

        return completableFuture;
    }

    @Override
    public boolean isDisabled() {
        return disabled || FlectonePulseAPI.isDisabling();
    }

    @Override
    public void onTick() {
        processTasks(currentTick.getAndIncrement());
    }

    private boolean isAsyncThread() {
        return Thread.currentThread().getName().startsWith(THREAD_PREFIX);
    }

    private void processTasks(long tick) {
        List<ScheduledTask> tasks = scheduledTasks.remove(tick);
        if (tasks == null) return;

        List<ScheduledTask> syncTasks = new ArrayList<>();

        for (ScheduledTask scheduledTask : tasks) {
            if (scheduledTask.async()) {
                execute(scheduledTask);
            } else {
                syncTasks.add(scheduledTask);
            }
        }

        syncTasks.forEach(this::execute);
    }

    private void execute(ScheduledTask scheduledTask) {
        if (scheduledTask.future().isCancelled()) return;

        Runnable runnable = wrapExceptionRunnable(scheduledTask);
        if (scheduledTask.async()) {
            execute(runnable);
        } else {
            runnable.run();
        }

        if (scheduledTask.isRepeating() && !scheduledTask.future().isCancelled()) {
            rescheduleTask(scheduledTask);
        }
    }

    private void execute(Runnable runnable) {
        try {
            executorService.execute(runnable);
        } catch (RejectedExecutionException _) {
            fLogger.warning("Executor overloaded, increase 'max_pool_size' or switch 'work_queue' to 'LINKED_BLOCKING' in config.yml. Running in current thread...");
            runnable.run();
        }
    }

    private Runnable wrapExceptionRunnable(ScheduledTask scheduledTask) {
        return wrapExceptionRunnable(scheduledTask.runnable(), scheduledTask.future());
    }

    protected Runnable wrapExceptionRunnable(SchedulerRunnable runnable, CompletableFuture<Void> future) {
        return () -> {
            try {
                runnable.run();
                if (!future.isCancelled()) {
                    future.complete(null);
                }
            } catch (Exception e) {
                fLogger.warning(e, "Task execution failed:");
                future.completeExceptionally(e);
            }
        };
    }

    private void rescheduleTask(ScheduledTask task) {
        registerTask(task.withNextTick(task.nextTick() + task.period()));
    }

    private ScheduledTask registerTask(SchedulerRunnable runnable, long nextTick, long period, boolean async) {
        return registerTask(new ScheduledTask(runnable, nextTick, period, async, new CompletableFuture<>()));
    }

    private ScheduledTask registerTask(ScheduledTask task) {
        scheduledTasks.compute(task.nextTick(), (_, tasks) -> {
            List<ScheduledTask> list = (tasks != null) ? tasks : new CopyOnWriteArrayList<>();
            list.add(task);
            return list;
        });

        return task;
    }

    private ExecutorService createExecutorService() {
        config = fileFacadeProvider.get().config().executor();

        ThreadFactory factory = Thread.ofPlatform()
                .name(THREAD_PREFIX, threadCounter.getAndIncrement())
                .factory();

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                config.minPoolSize(),
                config.maxPoolSize() == -1 ? Integer.MAX_VALUE : config.maxPoolSize(),
                config.keepAlive().duration(), config.keepAlive().timeUnit(),
                config.workQueue() == Config.Executor.WorkQueue.SYNCHRONOUS ? new SynchronousQueue<>() : new LinkedBlockingQueue<>(),
                factory
        );

        threadPoolExecutor.allowCoreThreadTimeOut(config.allowCoreThreadTimeout());

        return threadPoolExecutor;
    }

    @With
    private record ScheduledTask(
            SchedulerRunnable runnable,
            long nextTick,
            long period,
            boolean async,
            CompletableFuture<Void> future
    ) {

        boolean isRepeating() {
            return period > 0;
        }

    }
}