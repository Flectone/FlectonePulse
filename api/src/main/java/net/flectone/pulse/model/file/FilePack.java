package net.flectone.pulse.model.file;

import lombok.With;
import net.flectone.pulse.config.*;

import java.util.Map;

/**
 * One complete set of loaded config files. A reload swaps the whole set at once, so nothing ever
 * reads a half-updated configuration.
 *
 * @param command the command settings
 * @param config the general settings
 * @param integration the integration settings
 * @param message the message settings
 * @param permission the permission settings
 * @param localizations the localizations, keyed by locale name
 * @author TheFaser
 */
@With
public record FilePack(
        Command command,
        Config config,
        Integration integration,
        Message message,
        Permission permission,
        Map<String, Localization> localizations
) {
}
