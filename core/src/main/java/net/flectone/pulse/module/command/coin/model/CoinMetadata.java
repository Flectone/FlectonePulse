package net.flectone.pulse.module.command.coin.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.event.EventMetadata;

@Getter
@SuperBuilder
public class CoinMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    private final int percent;

}
