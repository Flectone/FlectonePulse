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

    private final long duration;

    private final float elevation;

    private final float interactionHeight;

    private final boolean interactionRiding;

    private final long creationTime = System.currentTimeMillis();

    private final List<FPlayer> viewers;

    @Setter
    private boolean created;

    public boolean isExpired() {
        return System.currentTimeMillis() > creationTime + duration;
    }
}