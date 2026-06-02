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
        boolean valid,
        String server
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
            @ColumnName("type") String typeName,
            @ColumnName("valid") boolean valid,
            @ColumnName("server") String server) {
        this(id, player, date, time, reason, moderator, Moderation.Type.valueOf(typeName.toUpperCase()), valid, server);
    }

    public boolean isActive() {
        return valid() && !isExpired();
    }

    public boolean isPermanent() {
        return time == PERMANENT_TIME;
    }

    public boolean isExpired() {
        return !isPermanent() && System.currentTimeMillis() > time;
    }

    public long getRemainingTime() {
        return isPermanent() ? PERMANENT_TIME : time - System.currentTimeMillis();
    }

    public long getOriginalTime() {
        return (Math.abs(date - time) + 500) / 1000 * 1000;
    }

    public boolean equals(Moderation moderation) {
        return this.id == moderation.id();
    }

    public enum Type {
        MUTE,
        MAINTENANCE,
        BAN,
        WARN,
        WHITELIST,
        KICK,
        UNMUTE,
        UNMAINTENANCE,
        UNBAN,
        UNWARN,
        UNWHITELIST
    }
}