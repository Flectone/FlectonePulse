package net.flectone.pulse.module.message.format.uniform.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageFormattedEvent;
import net.flectone.pulse.module.message.format.uniform.UniformModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseUniformListener implements PulseListener {

    private final UniformModule uniformModule;

    @Pulse
    public void onMessageFormattedEvent(MessageFormattedEvent event) {
        if (event.context().module() == ModuleName.MESSAGE_FORMAT_UNIFORM) return;

        uniformModule.markFormatted(event.component());
    }

}
