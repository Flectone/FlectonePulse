package net.flectone.pulse.model.entity;

import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.UUID;

/**
 * This is a platform-dynamic, FlectonePulse player
 * <hr>
 * <p>
 * For example, plugins using the Bukkit API can get an instance of the {@link FPlayer} object by simply using
 * <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Entity.html#getUniqueId()"><code>Entity.getUniqueId()</code></a>
 * and using {@link FPlayerService}'s <code>{@link UUID} getFPlayer</code> method.
 * </p>
 *
 * @see FPlayerService
 */
public interface FPlayer extends FEntity {

    /**
     * Starts building a player.
     *
     * @return a new builder
     */
    static FPlayerImpl.FPlayerImplBuilder builder() {
        return new FPlayerImpl.FPlayerImplBuilder();
    }

    /**
     * The database id carried by the unknown player.
     */
    int UNKNOWN_ID = Integer.MIN_VALUE;

    /**
     * The database id reserved for the console.
     */
    int CONSOLE_ID = -1;

    /**
     * The type of a real player.
     */
    String PLAYER_TYPE = "PLAYER";

    /**
     * The type of the console.
     */
    String CONSOLE_TYPE = "CONSOLE";

    /**
     * The type of a sender that came in through an integration such as Discord.
     */
    String INTEGRATION_TYPE = "INTEGRATION";

    /**
     * The stand-in used when the real player cannot be determined.
     */
    FPlayer UNKNOWN = FPlayer.builder().build();

    /**
     * The database id.
     *
     * @return the id
     */
    Integer id();

    /**
     * Whether this is the console rather than a player.
     *
     * @return true for the console
     */
    boolean isConsole();

    /**
     * Whether this sender came in through an integration.
     *
     * @return true for an integration sender
     */
    boolean isIntegration();

    /**
     * Whether the player is connected right now.
     *
     * @return true if online
     */
    boolean isOnline();

    /**
     * The address the player connected from.
     *
     * @return the address, or null if it is not known
     */
    String ip();

    /**
     * The extra name parts resolved once and carried along, so another server need not resolve them again.
     *
     * @return the constants
     */
    List<Component> constants();

    /**
     * Returns a copy with a different name.
     *
     * @param name the new name
     * @return the copy
     */
    FPlayer withName(String name);

    /**
     * Returns a copy with a different account id.
     *
     * @param uuid the new id
     * @return the copy
     */
    FPlayer withUuid(UUID uuid);

    /**
     * Returns a copy with a different online state.
     *
     * @param online the new state
     * @return the copy
     */
    FPlayer withOnline(boolean online);

    /**
     * Returns a copy with a different database id.
     *
     * @param id the new id
     * @return the copy
     */
    FPlayer withId(Integer id);

    /**
     * Returns a copy with a different address.
     *
     * @param ip the new address
     * @return the copy
     */
    FPlayer withIp(String ip);

    /**
     * Returns a copy with different constants.
     *
     * @param constants the new constants
     * @return the copy
     */
    FPlayer withConstants(List<Component> constants);

    /**
     * Starts a builder pre-filled with this player's values.
     *
     * @return the pre-filled builder
     */
    FPlayerImpl.FPlayerImplBuilder toBuilder();

    @Override
    default boolean isUnknown() {
        return id() < 0 && !isConsole();
    }


}