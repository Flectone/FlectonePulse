package net.flectone.pulse.scheduler;

import net.flectone.pulse.model.exception.SchedulerTaskException;

@FunctionalInterface
public interface SchedulerRunnable {

    void run() throws SchedulerTaskException;

}
