package net.flectone.pulse.platform.proxy;

import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FEntity;
import org.jspecify.annotations.NonNull;

/**
 * One way of talking to the other servers in a network, such as a plugin-message channel or Redis.
 * @author TheFaser
 */
public interface Proxy {

    /**
     * The plugin-message channel identifier used for BungeeCord / Velocity
     * communication between the proxy and all backend servers.
     */
    String CHANNEL = "flectonepulse:main";

    /**
     * Whether this transport is turned on in the config and ready to use.
     *
     * @return true if it can carry messages
     */
    boolean isEnable();

    /**
     * Opens the connection or registers the channel.
     */
    void onEnable();

    /**
     * Closes the connection or unregisters the channel.
     */
    void onDisable();

    /**
     * Sends an encoded message to the other servers.
     *
     * @param sender the entity the message is from
     * @param tag the module the message belongs to
     * @param message the encoded payload
     * @return true if it was handed to the transport
     */
    boolean sendMessage(@NonNull FEntity sender, @NonNull ModuleName tag, byte @NonNull [] message);

}