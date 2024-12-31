package net.flectone.pulse.ticker;

import com.google.inject.Inject;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class AbstractTicker {

    @Inject
    private ThreadManager threadManager;

    @Inject
    private FPlayerManager fPlayerManager;

    private final Consumer<FPlayer> consumer;
    private final Runnable runnable;

    public AbstractTicker(Consumer<FPlayer> consumer) {
        this(consumer, null);
    }

    public AbstractTicker(Runnable runnable) {
        this(null, runnable);
    }

    public AbstractTicker(@Nullable Consumer<FPlayer> consumer, @Nullable Runnable runnable) {
        this.consumer = consumer;
        this.runnable = runnable;
    }

    public void runTaskTimerAsync(int delay, int period) {
        if (consumer != null) {
            threadManager.runAsyncTimer(() -> fPlayerManager.getFPlayers().forEach(consumer), delay, period);
        }

        if (runnable != null) {
            threadManager.runAsyncTimer(runnable, delay, period);
        }
    }
}
