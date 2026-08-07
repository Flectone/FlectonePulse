package net.flectone.pulse.module.message.update.model;

import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * A notice that a newer plugin version exists.
 * Builds on {@link MessageContext}.
 * @author TheFaser
 */
public interface UpdateMessageContext extends MessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static UpdateMessageContextImpl.UpdateMessageContextImplBuilder builder() {
        return UpdateMessageContextImpl.builder();
    }

    /**
     * The version available.
     *
     * @return the version available
     */
    @NonNull String latestVersion();

    /**
     * The version running.
     *
     * @return the version running
     */
    @NonNull String currentVersion();

    /**
     * Returns a copy carrying a different value.
     *
     * @param latestVersion the version available
     * @return the copy
     */
    UpdateMessageContext withLatestVersion(@NonNull String latestVersion);

    /**
     * Returns a copy carrying a different value.
     *
     * @param currentVersion the version running
     * @return the copy
     */
    UpdateMessageContext withCurrentVersion(@NonNull String currentVersion);

}