package net.flectone.pulse.model.file;

import lombok.With;
import net.flectone.pulse.config.*;

import java.util.Map;

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
