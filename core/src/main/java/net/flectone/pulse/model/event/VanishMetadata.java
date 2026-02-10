package net.flectone.pulse.model.event;

import net.flectone.pulse.config.setting.LocalizationSetting;

public interface VanishMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    boolean ignoreVanish();

}
