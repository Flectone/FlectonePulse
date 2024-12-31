package net.flectone.pulse.scheduler;

public abstract class BlfRunnable implements BlfTask {

    boolean isCancelled;
    boolean isRepeating;
    long period;

    /**
     * Stops the delayed or repeating task from running next time.
     */
    public void cancel() {
        this.isCancelled = true;
    }

}
