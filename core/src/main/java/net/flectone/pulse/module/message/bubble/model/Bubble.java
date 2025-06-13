package net.flectone.pulse.module.message.bubble.model;

import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.model.FPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a single speech bubble message
 */
@Getter
public class Bubble {

    private final int id;
    private final UUID uuid;
    private final FPlayer sender;
    private final String rawMessage;
    private final long duration;
    private final long creationTime;
    private final int height;

    private final float interactionHeight;
    private final boolean interactionRiding;

    @Setter private boolean created;

    public Bubble(int id, @NotNull FPlayer sender, @NotNull String message, long duration, int height, float interactionHeight, boolean interactionRiding) {
        this.id = id;
        this.uuid = UUID.randomUUID();
        this.sender = sender;
        this.rawMessage = message;
        this.duration = duration;
        this.height = height;
        this.interactionHeight = interactionHeight;
        this.interactionRiding = interactionRiding;
        this.creationTime = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > creationTime + duration;
    }
}