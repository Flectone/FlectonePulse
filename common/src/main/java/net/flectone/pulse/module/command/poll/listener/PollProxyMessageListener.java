package net.flectone.pulse.module.command.poll.listener;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Range;
import net.flectone.pulse.module.command.poll.PollModule;
import net.flectone.pulse.module.command.poll.model.Poll;
import net.flectone.pulse.module.command.poll.model.PollMessageContext;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.payload.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PollProxyMessageListener implements PulseListener {

    private final PollModule pollModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;
    private final MessagePipeline messagePipeline;
    private final Gson gson;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_POLL) return event;
        if (!pollModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            PollModule.Action action = PollModule.Action.valueOf(proxyPayload.readString());
            switch (action) {
                case CREATE -> {
                    if (moduleController.isDisabledFor(pollModule, event.sender())) return event.withProcessed(true);

                    Poll poll = gson.fromJson(proxyPayload.readString(), Poll.class);
                    pollModule.saveAndUpdateLast(poll);

                    messageDispatcher.dispatch(pollModule, EventMetadata.builder()
                            .range(Range.get(Range.Type.SERVER))
                            .sound(pollModule.soundOrThrow())
                            .messageContext(fResolver -> PollMessageContext.builder()
                                    .base(MessageContext.builder()
                                            .uuid(event.uuid())
                                            .sender(event.sender())
                                            .receiver(fResolver)
                                            .message(pollModule.resolvePollFormat(fResolver, poll, PollModule.Status.START))
                                            .tagResolver(messagePipeline.messageTag(event.sender(), fResolver, poll.getTitle()))
                                            .tagResolvers(pollModule.resolveAnswerTags(event.sender(), fResolver, poll))
                                            .build()
                                    )
                                    .string(poll.getTitle())
                                    .poll(poll)
                                    .status(PollModule.Status.START)
                                    .action(PollModule.Action.CREATE)
                                    .build()
                            )
                            .build()
                    );
                }
                case VOTE -> pollModule.vote(event.sender(), proxyPayload.readInt(), proxyPayload.readInt(), event.uuid());
            }
        }

        return event.withProcessed(true);
    }

}
