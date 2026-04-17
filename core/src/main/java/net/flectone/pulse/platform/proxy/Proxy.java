package net.flectone.pulse.platform.proxy;

import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.util.constant.ModuleName;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface Proxy {

    boolean isEnable();

    void onEnable();

    void onDisable();

    boolean sendMessage(@NonNull FEntity sender, @NonNull ModuleName tag, byte @NonNull [] message, @Nullable EventMetadata<?> eventMetadata);

}