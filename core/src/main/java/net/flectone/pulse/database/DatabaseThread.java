package net.flectone.pulse.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.logger.FLogger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Singleton
public class DatabaseThread {

    private final Thread thread;
    private final BlockingQueue<Runnable> taskQueue;
    private final FLogger fLogger;

    @Inject
    public DatabaseThread(FLogger fLogger) {
        this.fLogger = fLogger;
        this.taskQueue = new LinkedBlockingQueue<>();
        this.thread = new Thread(this::run);
        this.thread.setName("FlectonePulse Thread");
        this.thread.setDaemon(true);
        this.thread.start();
    }

    public void execute(Runnable runnable) {
        try {
            taskQueue.put(runnable);
        } catch (InterruptedException e) {
            fLogger.warning(e);
        }
    }

    public void close() {
        try {
            thread.interrupt();
            thread.join();
        } catch (Exception e) {
            fLogger.warning(e);
        }
    }

    private void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Runnable task = taskQueue.take();
                task.run();
            }
        } catch (Exception e) {
            fLogger.warning(e);
        }
    }
}
