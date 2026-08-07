package net.flectone.pulse.scheduler;

import net.flectone.pulse.exception.SchedulerTaskException;

/**
 * A scheduled task that is allowed to fail without bringing down the scheduler thread.
 * @author TheFaser
 */
@FunctionalInterface
public interface SchedulerRunnable {

    /**
     * Runs the task.
     *
     * @throws SchedulerTaskException if the task fails
     */
    void run() throws SchedulerTaskException;

}
