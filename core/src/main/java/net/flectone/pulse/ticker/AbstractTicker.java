package net.flectone.pulse.ticker;

import com.google.inject.Inject;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FPlayer;

import java.util.function.Consumer;

public abstract class AbstractTicker {

    @Inject
    private ThreadManager threadManager;

    @Inject
    private FPlayerManager fPlayerManager;

    private final Consumer<FPlayer> consumer;

    public AbstractTicker(Consumer<FPlayer> consumer) {
        this.consumer = consumer;
    }

    public void runTaskTimerAsync(long delay, long period) {
        threadManager.runAsyncTimer(() -> fPlayerManager.getFPlayers().forEach(consumer), delay, period);
    }
}
