package net.flectone.pulse.execution.dispatcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.MessagePrepareEvent;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.platform.filter.RangeFilter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Dispatcher responsible for routing and sending messages to players.
 * Handles message formatting, filtering, and event dispatching through the messaging pipeline.
 *
 * @author TheFaser
 * @since 1.8.2
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessageDispatcher {

    private final FPlayerService fPlayerService;
    private final SocialService socialService;
    private final RangeFilter rangeFilter;
    private final MessagePipeline messagePipeline;
    private final EventDispatcher eventDispatcher;
    private final TaskScheduler taskScheduler;

    /**
     * Dispatches a message event to eligible receivers based on module localization settings.
     * This is a convenience method that extracts the module name from the localization object.
     *
     * @param module The module localization containing the module name and localization data
     * @param eventMetadata Metadata containing event information and filtering criteria
     * @return The set of players who received the message
     */
    public Set<FPlayer> dispatch(@NonNull ModuleLocalization module, @NonNull EventMetadata eventMetadata) {
        return dispatch(module.name(), eventMetadata);
    }

    /**
     * Dispatches a message event to eligible receivers determined by the module name and event metadata.
     * Automatically resolves the receiver list using {@link #createReceivers(ModuleName, EventMetadata)}.
     *
     * @param moduleName The module name used for localization and receiver filtering
     * @param eventMetadata Metadata containing event information and filtering criteria
     * @return The set of players who received the message
     */
    public Set<FPlayer> dispatch(@NonNull ModuleName moduleName, @NonNull EventMetadata eventMetadata) {
        return dispatch(moduleName, eventMetadata, createReceivers(moduleName, eventMetadata));
    }

    /**
     * Dispatches a message event to the specified set of receivers asynchronously.
     * Each receiver receives an individually created message event for personalized message processing.
     * Dispatching is skipped if the receiver set is empty.
     *
     * @param moduleName The module name used for message localization
     * @param eventMetadata Metadata containing event information for message creation
     * @param receivers The pre-filtered set of players to receive the message
     * @return The set of players who received the message (same as input receivers)
     */
    public Set<FPlayer> dispatch(@NonNull ModuleName moduleName, @NonNull EventMetadata eventMetadata, @NonNull Set<FPlayer> receivers) {
        if (!receivers.isEmpty()) {
            taskScheduler.runAsync(() -> receivers.forEach(fReceiver ->
                    dispatch(createMessageEvent(fReceiver, moduleName, eventMetadata)))
            );
        }

        return receivers;
    }

    /**
     * Directly dispatches a pre-built message send event through the event system.
     *
     * @param messageSendEvent The message send event to dispatch
     * @return The dispatched message send event
     */
    public MessageSendEvent dispatch(MessageSendEvent messageSendEvent) {
        return eventDispatcher.dispatch(messageSendEvent);
    }

    /**
     * Creates and populates the receiver list for the message event based on filtering criteria.
     * Applies player filters, range filters, and module settings checks to determine eligible receivers.
     *
     * @param moduleName The module name containing localization and settings to check for each receiver
     * @param eventMetadata Metadata containing event information and filtering criteria
     * @return A set of eligible players to receive the message, or an empty set if the event is for proxy and canceled
     */
    public Set<FPlayer> createReceivers(ModuleName moduleName, EventMetadata eventMetadata) {
        MessageContext rawMessageContext = eventMetadata.resolveMessageContext(FPlayer.UNKNOWN);

        MessagePrepareEvent messagePrepareEvent = eventDispatcher.dispatch(new MessagePrepareEvent(moduleName, eventMetadata, rawMessageContext));

        // if canceled, it means that message was sent to Proxy
        if (messagePrepareEvent.isForProxy() && messagePrepareEvent.cancelled()) return Set.of();

        Set<FPlayer> receivers = fPlayerService.getFPlayersWithConsole().stream()
                .filter(rangeFilter.createFilter(eventMetadata, rawMessageContext))
                .filter(fReceiver -> socialService.isSetting(fReceiver, moduleName))
                .collect(Collectors.toSet());

        if (messagePrepareEvent.receivers().isEmpty()) return receivers;

        Set<FPlayer> newReceivers = new HashSet<>(receivers);
        newReceivers.addAll(messagePrepareEvent.receivers());

        return Set.copyOf(newReceivers);
    }

    /**
     * Creates a complete message send event for a specific receiver.
     * Builds formatted message components including main message, format wrapper, and destination subtext.
     *
     * @param fReceiver The player receiving the message
     * @param moduleName The name identifier for the module
     * @param eventMetadata Metadata containing event information and message content
     * @return A fully constructed MessageSendEvent ready for dispatch
     */
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
