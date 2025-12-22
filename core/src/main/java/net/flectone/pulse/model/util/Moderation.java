package net.flectone.pulse.model.util;

import lombok.With;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

@With
public record Moderation(
        int id,
        int player,
        long date,
        long time,
        String reason,
        int moderator,
        Type type,
        boolean valid
) {
    public static final int PERMANENT_TIME = -1;

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
        this(id, player, date, time, reason, moderator, Moderation.Type.values()[typeOrdinal], valid);
    }

    public boolean isActive() {
        return valid() && !isExpired();
    }

    public boolean isPermanent() {
        return time == PERMANENT_TIME;
    }

    public boolean isExpired() {
        if (time == PERMANENT_TIME) return false;
        return System.currentTimeMillis() > time;
    }

    public long getRemainingTime() {
        if (time == PERMANENT_TIME) return PERMANENT_TIME;
        return time - System.currentTimeMillis();
    }

    public long getOriginalTime() {
        return (Math.abs(date - time) + 500) / 1000 * 1000;
    }

    public enum Type {
        MUTE,
        BAN,
        WARN,
        KICK
    }
}