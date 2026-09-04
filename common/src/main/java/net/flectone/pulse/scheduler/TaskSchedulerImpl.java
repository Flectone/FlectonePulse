package net.flectone.pulse.scheduler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.With;
import net.flectone.pulse.FlectonePulseAPI;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.LazyInstance;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Singleton
public class TaskSchedulerImpl implements TaskScheduler {

    private static final String THREAD_PREFIX = "FlectonePulse Thread #";
    private static final long STALL_CHECK_PERIOD_TICKS = 100L;

    private final AtomicLong currentTick = new AtomicLong(0L);
    private final AtomicLong threadCounter = new AtomicLong(0L);
    private final ConcurrentNavigableMap<Long, List<ScheduledTask>> scheduledTasks = new ConcurrentSkipListMap<>();
    private final Map<ModuleName, CompletableFuture<Void>> moduleChains = new ConcurrentHashMap<>();

    private final FLogger fLogger;
    private final LazyInstance<FPlayerService> fPlayerService;
    private final LazyInstance<FileFacade> fileFacade;

    @Getter
    private ExecutorService executorService;
    private Config.Executor config;

    private long completedTasks = -1L;
    private long stalledSince = 0L;

    private volatile boolean disabled = false;

    @Inject
    public TaskSchedulerImpl(FLogger fLogger,
                             LazyInstance<FPlayerService> fPlayerService,
                             LazyInstance<FileFacade> fileFacade) {
        this.fLogger = fLogger;
        this.fPlayerService = fPlayerService;
        this.fileFacade = fileFacade;
    }

    @Override
    public void reload() {
        processTasks(currentTick.get());
        scheduledTasks.clear();
    }

    @Override
    public void start() {
        disabled = false;
        executorService = createExecutorService();
    }

    @Override
    public void shutdown() {
        disabled = true;

        ExecutorService currentExecutorService = executorService;
        if (currentExecutorService == null) {
            scheduledTasks.clear();
            return;
        }

        processTasks(currentTick.get());

        currentExecutorService.shutdown();

        try {
            if (!currentExecutorService.awaitTermination(config.shutdownTimeout().duration(), config.shutdownTimeout().timeUnit())) {
                currentExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            fLogger.warning(e);
            currentExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            scheduledTasks.clear();
        }
    }

    @Override
    public CompletableFuture<Void> runAsync(SchedulerRunnable runnable) {
        if (isDisabled()) return runImmediately(runnable);
        if (isAsyncThread()) return runImmediately(runnable);

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        executorService.execute(wrapExceptionRunnable(runnable, completableFuture));

        return completableFuture;
    }

    @Override
    public CompletableFuture<Void> runAsync(ModuleName moduleName, SchedulerRunnable runnable) {
        if (isDisabled()) return runImmediately(runnable);

        ExecutorService currentExecutorService = executorService;
        return moduleChains.compute(moduleName, (_, chain) -> {
            CompletableFuture<Void> completableFuture = new CompletableFuture<>();
            Runnable task = wrapExceptionRunnable(runnable, completableFuture);

            if (chain == null || chain.isDone()) {
                currentExecutorService.execute(task);
            } else {
                chain.handle((_, _) -> null).thenRunAsync(task, currentExecutorService);
            }

            return completableFuture;
        });
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
        return runAsyncTimer(() -> fPlayerService.get().getPlatformFPlayers().forEach(fPlayerConsumer), delay, period);
    }

    @Override
    public <T> T await(CompletableFuture<T> future, T fallback, Duration timeout, String description) {
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException _) {
            future.cancel(false);

            fLogger.warning("%s did not answer within %s ms, continuing without it", description, timeout.toMillis());
        } catch (ExecutionException e) {
            fLogger.warning("%s failed", e.getCause(), description);
        } catch (CancellationException _) {
            // someone else gave up on the same task first
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }

        return fallback;
    }

    @Override
    public CompletableFuture<Void> runImmediately(SchedulerRunnable runnable) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        wrapExceptionRunnable(runnable, completableFuture).run();

        return completableFuture;
    }

    @Override
    public boolean isDisabled() {
        return disabled || FlectonePulseAPI.isDisabling() || executorService == null || executorService.isShutdown();
    }

    @Override
    public void onTick() {
        long tick = currentTick.getAndIncrement();

        processTasks(tick);

        if (tick % STALL_CHECK_PERIOD_TICKS == 0) {
            checkQueue();
        }
    }

    private void checkQueue() {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorService;

        long completed = threadPoolExecutor.getCompletedTaskCount();
        boolean stalled = !threadPoolExecutor.getQueue().isEmpty() && completed == completedTasks;

        completedTasks = completed;

        if (!stalled) {
            stalledSince = 0L;
            return;
        }

        long now = System.currentTimeMillis();
        if (stalledSince == 0L) {
            stalledSince = now;
            return;
        }

        long timeout = config.stallTimeout().timeUnit().toMillis(config.stallTimeout().duration());
        if (now - stalledSince < timeout) return;

        stalledSince = 0L;
        resetPool(timeout);
    }

    private void resetPool(long timeout) {
        fLogger.warning("No task has finished in %s seconds. Cancelling existing threads and starting a new ones", timeout / 1000L);

        completedTasks = -1L;

        Thread.getAllStackTraces().keySet().stream()
                .filter(thread -> thread.getName().startsWith(THREAD_PREFIX))
                .forEach(Thread::interrupt);
    }

    private boolean isAsyncThread() {
        return Thread.currentThread().getName().startsWith(THREAD_PREFIX);
    }

    private void processTasks(long tick) {
        NavigableMap<Long, List<ScheduledTask>> due = scheduledTasks.headMap(tick, true);

        while (!due.isEmpty()) {
            Map.Entry<Long, List<ScheduledTask>> entry = due.pollFirstEntry();
            if (entry == null) break;

            List<ScheduledTask> syncTasks = new ArrayList<>();

            for (ScheduledTask scheduledTask : entry.getValue()) {
                if (scheduledTask.async()) {
                    execute(scheduledTask, tick);
                } else {
                    syncTasks.add(scheduledTask);
                }
            }

            syncTasks.forEach(scheduledTask -> execute(scheduledTask, tick));
        }
    }

    private void execute(ScheduledTask scheduledTask, long currentProcessedTick) {
        if (scheduledTask.future().isCancelled()) return;

        Runnable runnable = wrapExceptionRunnable(scheduledTask);
        if (scheduledTask.async()) {
            executorService.execute(runnable);
        } else {
            runnable.run();
        }

        if (scheduledTask.isRepeating() && !scheduledTask.future().isCancelled()) {
            rescheduleTask(scheduledTask, currentProcessedTick);
        }
    }

    private Runnable wrapExceptionRunnable(ScheduledTask scheduledTask) {
        CompletableFuture<Void> future = scheduledTask.future();

        return () -> {
            if (future.isCancelled()) return;

            try {
                scheduledTask.runnable().run();

                // repeating task future is never completed, otherwise cancel() stops working
                if (!scheduledTask.isRepeating()) {
                    future.complete(null);
                }
            } catch (Exception e) {
                fLogger.warning("Task execution failed:", e);

                if (!scheduledTask.isRepeating()) {
                    future.completeExceptionally(e);
                }
            }
        };
    }

    protected Runnable wrapExceptionRunnable(SchedulerRunnable runnable, CompletableFuture<Void> future) {
        return () -> {
            if (future.isCancelled()) return;

            try {
                runnable.run();
                future.complete(null);
            } catch (Exception e) {
                fLogger.warning("Task execution failed:", e);
                future.completeExceptionally(e);
            }
        };
    }

    private void rescheduleTask(ScheduledTask task, long currentProcessedTick) {
        long nextTick = task.nextTick() + task.period();
        if (nextTick <= currentProcessedTick) {
            nextTick = currentProcessedTick + task.period();
        }

        registerTask(task.withNextTick(nextTick));
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
        config = fileFacade.get().config().executor();

        ThreadFactory factory = Thread.ofPlatform()
                .name(THREAD_PREFIX, threadCounter.getAndIncrement())
                .factory();

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                config.minPoolSize(),
                config.maxPoolSize() == -1 ? Integer.MAX_VALUE : config.maxPoolSize(),
                config.keepAlive().duration(), config.keepAlive().timeUnit(),
                config.workQueue() == Config.Executor.WorkQueue.SYNCHRONOUS ? new SynchronousQueue<>() : new LinkedBlockingQueue<>(),
                factory,
                (runnable, _) -> {
                    if (!isDisabled()) {
                        fLogger.warning("Executor overloaded, increase 'max_pool_size' or switch 'work_queue' to 'LINKED_BLOCKING' in config.yml. Running in current thread...");
                    }

                    runnable.run();
                }
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