package net.flectone.pulse.module.command.poll.listener;

import com.google.gson.Gson;
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
import net.flectone.pulse.module.command.poll.PollModule;
import net.flectone.pulse.module.command.poll.model.Poll;
import net.flectone.pulse.module.command.poll.model.PollMetadata;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PollProxyMessageListener implements PulseListener {

    private final PollModule pollModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;
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

                    messageDispatcher.dispatch(pollModule, PollMetadata.<Localization.Command.Poll>builder()
                            .base(EventMetadata.<Localization.Command.Poll>builder()
                                    .uuid(event.uuid())
                                    .sender(event.sender())
                                    .format(pollModule.resolvePollFormat(event.sender(), poll, PollModule.Status.START))
                                    .range(Range.get(Range.Type.SERVER))
                                    .message(poll.getTitle())
                                    .sound(pollModule.soundOrThrow())
                                    .build()
                            )
                            .poll(poll)
                            .build()
                    );
                }
                case VOTE -> pollModule.vote(event.sender(), proxyPayload.readInt(), proxyPayload.readInt(), event.uuid());
            }
        }

        return event.withProcessed(true);
    }

}
