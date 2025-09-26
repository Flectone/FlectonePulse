package net.flectone.pulse.module.command.maintenance.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.model.event.EventMetadata;

@Getter
@SuperBuilder
public class MaintenanceMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    private final boolean turned;

}
