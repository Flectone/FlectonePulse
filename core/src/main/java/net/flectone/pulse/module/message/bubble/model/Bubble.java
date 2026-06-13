package net.flectone.pulse.module.message.bubble.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.model.entity.FPlayer;

import java.util.List;
import java.util.UUID;

/**
 * Represents a single speech bubble message
 */
@Getter
@SuperBuilder
public class Bubble {

    private final int id;

    private final UUID uuid;

    private final FPlayer sender;

    private final String rawMessage;

    // Full, un-split message text in the sender's language. Used by the renderer to
    // translate the whole message once per viewer-locale and re-split the translation.
    private final String fullMessage;

    // Sender's locale (source language for translation). May be null.
    private final String senderLocale;

    // Index of this chunk within its message (chunks of one message share the same id),
    // and the total number of original chunks. The renderer renders the whole re-split
    // stack on chunkIndex == 0 and treats chunkIndex > 0 as a no-op.
    @lombok.Builder.Default
    private final int chunkIndex = 0;

    @lombok.Builder.Default
    private final int chunkCount = 1;

    private final long duration;

    private final float elevation;

    private final float interactionHeight;

    private final boolean interactionRiding;

    private final long creationTime = System.currentTimeMillis();

    private final List<FPlayer> viewers;

    @Setter
    private boolean created;

    public boolean isExpired() {
        return System.currentTimeMillis() > getExpireTime();
    }

    public long getExpireTime() {
        return creationTime + duration;
    }

    public boolean equals(Bubble bubble) {
        return this.id == bubble.getId();
    }

}