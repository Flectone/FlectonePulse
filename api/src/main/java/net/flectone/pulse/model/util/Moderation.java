package net.flectone.pulse.model.util;

import lombok.With;

/**
 * A punishment issued by FlectonePulse, such as a ban, mute, warn, kick or whitelist entry.
 *
 * @param id the database id
 * @param player the punished player id
 * @param date when the punishment was issued, in milliseconds
 * @param time when it expires, in milliseconds, or {@link #PERMANENT_TIME}
 * @param reason the stated reason, may be null
 * @param moderator the issuing player id
 * @param type what kind of punishment this is
 * @param valid whether it has not been revoked
 * @param server the server it was issued on
 * @author TheFaser
 */
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

    /**
     * The expiry value marking a punishment that never runs out.
     */
    public static final int PERMANENT_TIME = -1;

    /**
     * Whether this punishment still applies, meaning it is neither revoked nor expired.
     *
     * @return true while the punishment is in force
     */
    public boolean isActive() {
        return valid() && !isExpired();
    }

    /**
     * Whether this punishment never expires.
     *
     * @return true if permanent
     */
    public boolean isPermanent() {
        return time == PERMANENT_TIME;
    }

    /**
     * Whether the expiry time has passed. Permanent punishments never expire.
     *
     * @return true if the punishment has run out
     */
    public boolean isExpired() {
        return !isPermanent() && System.currentTimeMillis() > time;
    }

    /**
     * Milliseconds left before this punishment expires.
     *
     * @return the remaining time, or {@link #PERMANENT_TIME} if permanent
     */
    public long getRemainingTime() {
        return isPermanent() ? PERMANENT_TIME : time - System.currentTimeMillis();
    }

    /**
     * The full length the punishment was issued for, rounded to whole seconds.
     *
     * @return the original duration in milliseconds
     */
    public long getOriginalTime() {
        return (Math.abs(date - time) + 500) / 1000 * 1000;
    }

    /**
     * Compares by database id only, ignoring fields that change as the punishment ages.
     *
     * @param moderation the punishment to compare against
     * @return true if both refer to the same record
     */
    public boolean equals(Moderation moderation) {
        return this.id == moderation.id();
    }

    /**
     * The kind of punishment, including the actions that revoke one.
     */
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