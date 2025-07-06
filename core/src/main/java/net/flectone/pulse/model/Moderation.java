package net.flectone.pulse.model;


import lombok.Getter;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

@Getter
public class Moderation {

    public static final int PERMANENT_TIME = -1;

    private final int id;
    private final int player;
    private final long date;
    private final long time;
    private final String reason;
    private final int moderator;
    private final Type type;
    private boolean valid;

    @JdbiConstructor
    public Moderation(
            @ColumnName("id") int id,
            @ColumnName("player") int player,
            @ColumnName("date") long date,
            @ColumnName("time") long time,
            @ColumnName("reason") String reason,
            @ColumnName("moderator") int moderator,
            @ColumnName("type") int typeOrdinal,
            @ColumnName("valid") boolean valid) {
        this.id = id;
        this.player = player;
        this.date = date;
        this.time = time;
        this.reason = reason;
        this.moderator = moderator;
        this.type = Moderation.Type.values()[typeOrdinal];
        this.valid = valid;
    }

    public Moderation(int id, int player, long date, long time, String reason, int moderator, Moderation.Type type, boolean valid) {
        this.id = id;
        this.player = player;
        this.date = date;
        this.time = time;
        this.reason = reason;
        this.moderator = moderator;
        this.type = type;
        this.valid = valid;
    }

    public boolean isActive() {
        return isValid() && !isExpired();
    }

    public void setInvalid() {
        this.valid = false;
    }

    public boolean isPermanent() {
        return time == -1;
    }

    public boolean isExpired() {
        if (time == -1) return false;

        return System.currentTimeMillis() > time;
    }

    public long getRemainingTime() {
        if (time == PERMANENT_TIME) return PERMANENT_TIME;
        return time - System.currentTimeMillis();
    }

    public long getOriginalTime() {
        return (Math.abs(date - time) + 500) / 1000 * 1000;
    }

    public boolean equals(Moderation moderation) {
        return this.id == moderation.getId();
    }

    public enum Type {
        MUTE,
        BAN,
        WARN,
        KICK
    }

}
