package net.flectone.pulse.module.message.bubble.model;

import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.model.entity.FPlayer;

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
    private final int elevation;

    private final float interactionHeight;
    private final boolean interactionRiding;

    @Setter private boolean created;

    Bubble(Builder builder) {
        this.id = builder.id;
        this.uuid = UUID.randomUUID();
        this.sender = builder.sender;
        this.rawMessage = builder.message;
        this.duration = builder.duration;
        this.elevation = builder.elevation;
        this.interactionHeight = builder.interactionHeight;
        this.interactionRiding = builder.interactionRiding;
        this.creationTime = System.currentTimeMillis();
    }

    public static class Builder {

        private int id;
        private FPlayer sender;
        private String message;
        private long duration;
        private int elevation;
        private float interactionHeight;
        private boolean interactionRiding;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder sender(FPlayer sender) {
            this.sender = sender;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder duration(long duration) {
            this.duration = duration;
            return this;
        }

        public Builder elevation(int elevation) {
            this.elevation = elevation;
            return this;
        }

        public Builder interactionHeight(float interactionHeight) {
            this.interactionHeight = interactionHeight;
            return this;
        }

        public Builder interactionRiding(boolean interactionRiding) {
            this.interactionRiding = interactionRiding;
            return this;
        }

        public Bubble build() {
            return new Bubble(this);
        }
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > creationTime + duration;
    }
}