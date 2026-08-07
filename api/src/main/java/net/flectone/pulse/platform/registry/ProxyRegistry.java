package net.flectone.pulse.platform.registry;

import net.flectone.pulse.platform.proxy.Proxy;

import java.util.List;
import java.util.function.Predicate;

/**
 * Owns the transports used to reach the other servers.
 * @author TheFaser
 */
public interface ProxyRegistry extends Registry {

    /**
     * Every registered transport, enabled or not.
     *
     * @return the transports
     */
    List<Proxy> getProxies();

    /**
     * Whether at least one transport is usable, which decides if messages leave this server at all.
     *
     * @return true if any transport is enabled
     */
    boolean hasEnabledProxy();

    /**
     * Whether at least one usable transport matches the test.
     *
     * @param proxyPredicate the test
     * @return true if a matching transport is enabled
     */
    boolean hasEnabledProxy(Predicate<Proxy> proxyPredicate);

    /**
     * Adds a transport.
     *
     * @param proxy the transport to add
     */
    void registry(Proxy proxy);

    /**
     * Closes every transport and opens the ones the current config enables.
     */
    void reload();

}
