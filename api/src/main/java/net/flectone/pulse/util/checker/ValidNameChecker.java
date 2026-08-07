package net.flectone.pulse.util.checker;

import org.jspecify.annotations.Nullable;

/**
 * Checks that a name could belong to a real account before it is used in a lookup.
 * @author TheFaser
 */
public interface ValidNameChecker {

    /**
     * Checks a name.
     *
     * @param name the name to check, may be null
     * @return true if the name is usable
     */
    boolean check(@Nullable String name);

}
