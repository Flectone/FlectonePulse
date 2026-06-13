package net.flectone.pulse.module.command.ball.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.ball.BallModule;
import net.flectone.pulse.module.command.ball.model.BallMetadata;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BallProxyMessageListener implements PulseListener {

    private final BallModule ballModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_BALL) return event;
        if (moduleController.isDisabledFor(ballModule, event.sender())) return event.withProcessed(true);
        if (!ballModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            int answer = proxyPayload.readInt();
            String message = proxyPayload.readString();

            messageDispatcher.dispatch(ballModule, BallMetadata.<Localization.Command.Ball>builder()
                    .base(EventMetadata.<Localization.Command.Ball>builder()
                            .uuid(event.uuid())
                            .sender(event.sender())
                            .format(ballModule.replaceAnswer(answer))
                            .message(message)
                            .destination(ballModule.config().destination())
                            .range(Range.get(Range.Type.SERVER))
                            .sound(ballModule.soundOrThrow())
                            .build()
                    )
                    .answer(answer)
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
