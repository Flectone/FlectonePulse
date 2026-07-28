package net.flectone.pulse.module.command.coin.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.coin.CoinModule;
import net.flectone.pulse.module.command.coin.model.CoinMetadata;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CoinProxyMessageListener implements PulseListener {

    private final CoinModule coinModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_COIN) return event;
        if (!moduleController.isEnable(coinModule)) return event.withProcessed(true);
        if (!coinModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            int percent = proxyPayload.readInt();

            messageDispatcher.dispatch(coinModule, CoinMetadata.builder()
                    .base(EventMetadata.builder()
                            .range(Range.get(Range.Type.SERVER))
                            .destination(coinModule.config().destination())
                            .sound(coinModule.soundOrThrow())
                            .messageContext(fResolver -> MessageContext.builder()
                                    .uuid(event.uuid())
                                    .sender(event.sender())
                                    .receiver(fResolver)
                                    .message(coinModule.replaceResult(fResolver, percent))
                                    .build()
                            )
                            .build()
                    )
                    .percent(percent)
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
