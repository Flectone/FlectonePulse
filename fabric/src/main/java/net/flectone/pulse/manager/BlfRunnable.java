package net.flectone.pulse.manager;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BlfRunnable implements BlfTask {

    private boolean isCancelled;
    private boolean isRepeating;
    private long period;

    /**
     * Stops the delayed or repeating task from running next time.
     */
    public void cancel() {
        this.isCancelled = true;
    }

}
