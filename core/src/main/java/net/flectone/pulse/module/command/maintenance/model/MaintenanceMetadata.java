package net.flectone.pulse.module.command.maintenance.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;

@Getter
@SuperBuilder
public class MaintenanceMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    private final boolean turned;

}
