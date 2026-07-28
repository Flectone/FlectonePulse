package net.flectone.pulse.model.event.message.context;

import net.flectone.pulse.model.util.ExternalModeration;
import org.jspecify.annotations.NonNull;

public interface ExternalModerationMessageContext extends MessageContext {

    static ExternalModerationMessageContextImpl.ExternalModerationMessageContextImplBuilder builder() {
        return ExternalModerationMessageContextImpl.builder();
    }

    @NonNull ExternalModeration externalModeration();

    ExternalModerationMessageContext withExternalModeration(@NonNull ExternalModeration externalModeration);

}