package net.flectone.pulse.execution.scheduler;

import net.flectone.pulse.exception.SchedulerTaskException;

@FunctionalInterface
public interface SchedulerRunnable {

    void run() throws SchedulerTaskException;

}
