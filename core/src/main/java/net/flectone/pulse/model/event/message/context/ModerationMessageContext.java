package net.flectone.pulse.model.event.message.context;

import org.jspecify.annotations.NonNull;
import net.flectone.pulse.model.util.Moderation;

public interface ModerationMessageContext extends MessageContext {

    static ModerationMessageContextImpl.ModerationMessageContextImplBuilder builder() {
        return ModerationMessageContextImpl.builder();
    }

    @NonNull Moderation moderation();

    ModerationMessageContext withModeration(@NonNull Moderation moderation);

}