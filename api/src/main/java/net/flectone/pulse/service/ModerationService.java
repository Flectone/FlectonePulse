package net.flectone.pulse.service;

import net.flectone.pulse.config.setting.ViolationSetting;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.Moderation;
import net.flectone.pulse.module.ModuleSimple;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Issues, revokes and looks up punishments, and tracks the violation counters that decide
 * when a chat rule turns into an automatic punishment.
 * @author TheFaser
 */
public interface ModerationService {

    /**
     * Drops every cached punishment.
     */
    void invalidate();

    /**
     * Drops the cached punishments of one player.
     *
     * @param uuid the player id
     */
    void invalidate(UUID uuid);

    /**
     * Drops one cached punishment and tells the other servers to do the same.
     *
     * @param fTarget the punished player
     * @param type the punishment type
     * @param id the punishment id
     */
    void invalidate(FPlayer fTarget, Moderation.Type type, int id);

    /**
     * Drops one cached punishment, optionally scoped to a single server.
     *
     * @param fTarget the punished player
     * @param type the punishment type
     * @param id the punishment id
     * @param server the server it applies to, or null for every server
     */
    void invalidate(FPlayer fTarget, Moderation.Type type, int id, @Nullable String server);

    /**
     * Drops the cached punishments of one player for one punishment type.
     *
     * @param uuid the player id
     * @param type the punishment type
     */
    void invalidate(UUID uuid, Moderation.Type type);

    /**
     * Bans a player.
     *
     * @param fPlayer the player to ban
     * @param time when the ban expires in milliseconds, or {@link Moderation#PERMANENT_TIME}
     * @param reason the stated reason
     * @param moderator the issuing player id
     * @return the stored punishment, or null if it could not be saved
     */
    @Nullable
    Moderation ban(FPlayer fPlayer, long time, String reason, int moderator);

    /**
     * Mutes a player.
     *
     * @param fPlayer the player to mute
     * @param time when the mute expires in milliseconds, or {@link Moderation#PERMANENT_TIME}
     * @param reason the stated reason
     * @param moderator the issuing player id
     * @return the stored punishment, or null if it could not be saved
     */
    @Nullable
    Moderation mute(FPlayer fPlayer, long time, String reason, int moderator);

    /**
     * Puts the server into maintenance, which keeps everyone but the allowed players out.
     *
     * @param fPlayer the player the entry is recorded against
     * @param time when maintenance ends in milliseconds, or {@link Moderation#PERMANENT_TIME}
     * @param reason the stated reason
     * @param moderator the issuing player id
     * @return the stored entry, or null if it could not be saved
     */
    @Nullable
    Moderation maintenance(FPlayer fPlayer, long time, String reason, int moderator);

    /**
     * Warns a player.
     *
     * @param fPlayer the player to warn
     * @param time when the warning expires in milliseconds, or {@link Moderation#PERMANENT_TIME}
     * @param reason the stated reason
     * @param moderator the issuing player id
     * @return the stored punishment, or null if it could not be saved
     */
    @Nullable
    Moderation warn(FPlayer fPlayer, long time, String reason, int moderator);

    /**
     * Kicks a player. A kick has no duration, so it is only recorded for the history.
     *
     * @param fPlayer the player to kick
     * @param reason the stated reason
     * @param moderator the issuing player id
     * @return the stored entry, or null if it could not be saved
     */
    @Nullable
    Moderation kick(FPlayer fPlayer, String reason, int moderator);

    /**
     * Adds a player to the whitelist.
     *
     * @param fPlayer the player to allow in
     * @param time when the entry expires in milliseconds, or {@link Moderation#PERMANENT_TIME}
     * @param reason the stated reason
     * @param moderator the issuing player id
     * @return the stored entry, or null if it could not be saved
     */
    @Nullable
    Moderation whitelist(FPlayer fPlayer, long time, String reason, int moderator);

    /**
     * Whether a player has an active punishment of the given type.
     *
     * @param fTarget the player to check
     * @param type the punishment type
     * @return true if one is in force
     */
    boolean hasValid(FPlayer fTarget, Moderation.Type type);

    /**
     * Whether one specific punishment is still in force.
     *
     * @param fTarget the player to check
     * @param type the punishment type
     * @param id the punishment id
     * @return true if it is in force
     */
    boolean hasValid(FPlayer fTarget, Moderation.Type type, int id);

    /**
     * The active punishment of a type for a player.
     *
     * @param fPlayer the player to check
     * @param type the punishment type
     * @return the punishment, or empty if there is none
     */
    Optional<Moderation> getValid(FPlayer fPlayer, Moderation.Type type);

    /**
     * A page of a player's active punishments on one server, for the list commands.
     *
     * @param fPlayer the player to check
     * @param type the punishment type
     * @param server the server to list from, or null for every server
     * @param limit how many to return
     * @param offset how many to skip
     * @return the page
     */
    List<Moderation> getValid(FPlayer fPlayer, Moderation.Type type, String server, int limit, int offset);

    /**
     * A page of a player's active punishments, for the list commands.
     *
     * @param fPlayer the player to check
     * @param type the punishment type
     * @param limit how many to return
     * @param offset how many to skip
     * @return the page
     */
    List<Moderation> getValid(FPlayer fPlayer, Moderation.Type type, int limit, int offset);

    /**
     * A page of every active punishment of a type, across all players.
     *
     * @param type the punishment type
     * @param limit how many to return
     * @param offset how many to skip
     * @return the page
     */
    List<Moderation> getValid(Moderation.Type type, int limit, int offset);

    /**
     * One active punishment by id.
     *
     * @param type the punishment type
     * @param id the punishment id
     * @return the punishment, or empty if it is not active
     */
    Optional<Moderation> getValid(Moderation.Type type, int id);

    /**
     * The names of everyone with an active punishment of a type, used for command suggestions.
     *
     * @param type the punishment type
     * @return the player names
     */
    List<String> getValidNames(Moderation.Type type);

    /**
     * How many active punishments of a type a player has, used to size the list pages.
     *
     * @param fPlayer the player to check
     * @param type the punishment type
     * @param server the server to count on, or null for every server
     * @return the count
     */
    int getTotalValidCount(FPlayer fPlayer, Moderation.Type type, @Nullable String server);

    /**
     * How many active punishments of a type exist in total.
     *
     * @param type the punishment type
     * @param server the server to count on, or null for every server
     * @return the count
     */
    int getTotalValidCount(Moderation.Type type, @Nullable String server);

    /**
     * Stores a punishment of any type, issued as of now.
     *
     * @param fPlayer the punished player
     * @param time when it expires in milliseconds, or {@link Moderation#PERMANENT_TIME}
     * @param reason the stated reason
     * @param moderator the issuing player id
     * @param type the punishment type
     * @return the stored punishment, or null if it could not be saved
     */
    @Nullable
    Moderation add(FPlayer fPlayer, long time, String reason, int moderator, Moderation.Type type);

    /**
     * Stores a punishment with an explicit issue date and server, used when replaying one that
     * arrived from another server.
     *
     * @param fPlayer the punished player
     * @param date when it was issued, in milliseconds
     * @param time when it expires in milliseconds, or {@link Moderation#PERMANENT_TIME}
     * @param reason the stated reason
     * @param moderator the issuing player id
     * @param type the punishment type
     * @param server the server it applies to, or null for every server
     * @return the stored punishment, or null if it could not be saved
     */
    @Nullable
    Moderation add(FPlayer fPlayer, long date, long time, String reason, int moderator, Moderation.Type type, @Nullable String server);

    /**
     * Records that a player broke a chat rule, which may trip the module's violation limit.
     *
     * @param uuid the player id
     * @param moduleSimple the module whose rule was broken
     * @param violationSetting the limit settings
     */
    void addViolation(UUID uuid, ModuleSimple moduleSimple, ViolationSetting violationSetting);

    /**
     * Records a violation directly, used when the count arrives from another server.
     *
     * @param violationKey the player and module the count belongs to
     * @param violationValue when the violation happened, in milliseconds
     */
    void addViolation(ViolationKey violationKey, Long violationValue);

    /**
     * Whether a player has broken a rule often enough to be punished for it.
     *
     * @param uuid the player id
     * @param moduleSimple the module whose rule was broken
     * @param violationSetting the limit settings
     * @return true if the limit has been reached
     */
    boolean isViolationRestricted(UUID uuid, ModuleSimple moduleSimple, ViolationSetting violationSetting);

    /**
     * When the current run of violations started, which is where the limit window is measured from.
     *
     * @param uuid the player id
     * @param moduleSimple the module whose rule was broken
     * @return the timestamp in milliseconds, or null if there are no violations on record
     */
    Long getFirstViolationTimestamp(UUID uuid, ModuleSimple moduleSimple);

    /**
     * Revokes a punishment.
     *
     * @param fModerator who is revoking it
     * @param fTarget the punished player
     * @param type the punishment type
     * @param id the punishment id, or -1 for the most recent one
     * @param reason the stated reason, may be null
     * @return the revoked punishment, or null if there was nothing to revoke
     */
    @Nullable
    Moderation remove(FPlayer fModerator, FPlayer fTarget, Moderation.Type type, int id, @Nullable String reason);

    /**
     * Revokes a punishment on one server.
     *
     * @param fModerator who is revoking it
     * @param fTarget the punished player
     * @param type the punishment type
     * @param id the punishment id, or -1 for the most recent one
     * @param reason the stated reason, may be null
     * @param server the server to revoke it on, or null for every server
     * @return the revoked punishment, or null if there was nothing to revoke
     */
    @Nullable
    Moderation remove(FPlayer fModerator, FPlayer fTarget, Moderation.Type type, int id, @Nullable String reason, @Nullable String server);

    /**
     * Revokes a punishment as of a given moment, so the record keeps the real end time.
     *
     * @param fModerator who is revoking it
     * @param fTarget the punished player
     * @param type the punishment type
     * @param time when it stopped applying, in milliseconds
     * @param id the punishment id, or -1 for the most recent one
     * @param reason the stated reason, may be null
     * @return the revoked punishment, or null if there was nothing to revoke
     */
    @Nullable
    Moderation remove(FPlayer fModerator, FPlayer fTarget, Moderation.Type type, long time, int id, @Nullable String reason);

    /**
     * Revokes a punishment as of a given moment on one server.
     *
     * @param fModerator who is revoking it
     * @param fTarget the punished player
     * @param type the punishment type
     * @param time when it stopped applying, in milliseconds
     * @param id the punishment id, or -1 for the most recent one
     * @param reason the stated reason, may be null
     * @param server the server to revoke it on, or null for every server
     * @return the revoked punishment, or null if there was nothing to revoke
     */
    @Nullable
    Moderation remove(FPlayer fModerator, FPlayer fTarget, Moderation.Type type, long time, int id, @Nullable String reason, @Nullable String server);

    /**
     * Whether a moderator may issue a punishment of this length, given the per-group limits.
     *
     * @param fPlayer the issuing moderator
     * @param time the requested duration in milliseconds
     * @param timeLimits the longest duration each group may issue
     * @return true if the duration is allowed
     */
    boolean isAllowedTime(FPlayer fPlayer, long time, Map<Integer, Long> timeLimits);

    /**
     * Whether one player outranks another, which decides if they may punish them.
     *
     * @param source the acting player
     * @param target the player being acted on
     * @return true if the acting player ranks higher
     */
    boolean hasHigherGroupThan(FPlayer source, FPlayer target);

    /**
     * The server name punishments of this type are recorded against, either this server or a
     * shared name when the punishment is network-wide.
     *
     * @param type the punishment type
     * @return the server name
     */
    String getServer(Moderation.Type type);

    /**
     * Identifies a violation counter by one player against one module.
     *
     * @param sender the player id
     * @param module the module whose rule was broken
     */
    record ViolationKey(
            UUID sender,
            ModuleName module
    ){}
}
