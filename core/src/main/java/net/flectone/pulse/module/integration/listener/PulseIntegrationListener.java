package net.flectone.pulse.module.integration.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.lifecycle.DisableEvent;
import net.flectone.pulse.model.event.lifecycle.EnableEvent;
import net.flectone.pulse.model.event.message.MessagePrepareEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.util.constant.ModuleName;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseIntegrationListener implements PulseListener {

    private final EventDispatcher eventDispatcher;

    @Pulse
    public void onEnableEvent(EnableEvent event) {
        if (event.type() != EnableEvent.Type.READY) return;

        eventDispatcher.dispatch(new MessagePrepareEvent(ModuleName.SERVER_ENABLE, EventMetadata.builder()
                .messageContext(_ -> MessageContext.builder()
                        .sender(FPlayer.UNKNOWN)
                        .message(ModuleName.SERVER_ENABLE.name())
                        .build()
                )
                .integration()
                .build())
        );
    }

    @Pulse
    public void onDisableEvent(DisableEvent event) {
        eventDispatcher.dispatch(new MessagePrepareEvent(ModuleName.SERVER_DISABLE, EventMetadata.builder()
                .messageContext(_ -> MessageContext.builder()
                        .sender(FPlayer.UNKNOWN)
                        .message(ModuleName.SERVER_DISABLE.name())
                        .build()
                )
                .integration()
                .build())
        );
    }

}
