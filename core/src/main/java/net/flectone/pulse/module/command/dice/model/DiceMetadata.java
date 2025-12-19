package net.flectone.pulse.module.command.dice.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;

import java.util.List;

@Getter
@SuperBuilder
public class DiceMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    @NonNull
    private final List<Integer> cubes;

}
