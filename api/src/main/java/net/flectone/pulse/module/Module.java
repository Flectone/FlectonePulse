package net.flectone.pulse.module;

import net.flectone.pulse.config.Config;

/**
 * The root module. Everything else hangs off it, so disabling this one disables the plugin's features.
 * @author TheFaser
 */
public interface Module extends ModuleSimple {

    @Override
    Config.Internal config();

}
