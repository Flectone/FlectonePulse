package net.flectone.pulse.module.integration.telegram.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.model.event.EventMetadata;
import org.jetbrains.annotations.NotNull;

@Getter
@SuperBuilder
public class TelegramMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @NonNull
    private final String userName;

    @NotNull
    private final String firstName;

    @NotNull
    private final String lastName;

    @NonNull
    private final String chat;

}
