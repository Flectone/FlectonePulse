package net.flectone.pulse.dispatcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.IntegrationMessageFormat;
import net.flectone.pulse.model.event.message.MessagePrepareEvent;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Destination;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.pipeline.MessagePipelineImpl;
import net.flectone.pulse.platform.filter.RangeFilter;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessageDispatcherImpl implements MessageDispatcher {

    private final FPlayerService fPlayerService;
    private final SocialService socialService;
    private final RangeFilter rangeFilter;
    private final MessagePipelineImpl messagePipeline;
    private final EventDispatcher eventDispatcher;
    private final TaskScheduler taskScheduler;

    @Override
    public Set<FPlayer> dispatch(@NonNull ModuleLocalization module, @NonNull EventMetadata eventMetadata) {
        return dispatch(module.name(), eventMetadata);
    }

    @Override
    public Set<FPlayer> dispatch(@NonNull ModuleName moduleName, @NonNull EventMetadata eventMetadata) {
        return dispatch(moduleName, eventMetadata, createReceivers(moduleName, eventMetadata));
    }

    @Override
    public Set<FPlayer> dispatch(@NonNull ModuleName moduleName, @NonNull EventMetadata eventMetadata, @NonNull Set<FPlayer> receivers) {
        if (!receivers.isEmpty()) {
            taskScheduler.runAsync(() -> receivers.forEach(fReceiver ->
                    dispatch(createMessageEvent(fReceiver, moduleName, eventMetadata)))
            );
        }

        return receivers;
    }

    @Override
    public MessageSendEvent dispatch(MessageSendEvent messageSendEvent) {
        return eventDispatcher.dispatch(messageSendEvent);
    }

    @Override
    public Set<FPlayer> createReceivers(ModuleName moduleName, EventMetadata eventMetadata) {
        MessageContext rawMessageContext = eventMetadata.resolveMessageContext(FPlayer.UNKNOWN);
        IntegrationMessageFormat integrationMessageFormat = eventMetadata.resolveIntegrationMessageFormat();

        MessagePrepareEvent messagePrepareEvent = eventDispatcher.dispatch(new MessagePrepareEvent(moduleName, eventMetadata, rawMessageContext, integrationMessageFormat));

        // if cancelled, it means that message was sent to Proxy
        if (eventMetadata.proxy() != null && messagePrepareEvent.cancelled()) return Set.of();

        Set<FPlayer> receivers = fPlayerService.getFPlayersWithConsole().stream()
                .filter(rangeFilter.createFilter(eventMetadata, rawMessageContext))
                .filter(fReceiver -> socialService.isSetting(fReceiver, moduleName))
                .collect(Collectors.toSet());

        if (messagePrepareEvent.receivers().isEmpty()) return receivers;

        Set<FPlayer> newReceivers = new HashSet<>(receivers);
        newReceivers.addAll(messagePrepareEvent.receivers());

        return Set.copyOf(newReceivers);
    }

    @Override
    public MessageSendEvent createMessageEvent(FPlayer fReceiver, ModuleName moduleName, EventMetadata eventMetadata) {
        MessageContext messageContext = eventMetadata.resolveMessageContext(fReceiver);

        Component messageComponent = messagePipeline.build(messageContext);

        Destination destination = eventMetadata.destination();

        // destination subtext
        Component messageSubComponent = StringUtils.isEmpty(destination.subtext())
                ? Component.empty()
                : messagePipeline.build(messageContext.withMessage(destination.subtext()));

        return new MessageSendEvent(
                moduleName,
                messageComponent,
                messageSubComponent,
                eventMetadata,
                messageContext
        );
    }

}
