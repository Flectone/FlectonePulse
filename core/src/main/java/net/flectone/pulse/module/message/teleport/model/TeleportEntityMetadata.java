package net.flectone.pulse.module.message.teleport.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.Localization;

@Getter
@SuperBuilder
public class TeleportEntityMetadata <L extends Localization.Localizable> extends TeleportMetadata<L> {

    @NonNull
    private final TeleportEntity teleportEntity;

}
