package net.flectone.pulse.dispatcher;

import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.module.ModuleLocalization;
import org.jspecify.annotations.NonNull;

import java.util.Set;

/**
 * Dispatcher responsible for routing and sending messages to players.
 * Handles message formatting, filtering, and event dispatching through the messaging pipeline.
 *
 * @author TheFaser
 * @since 1.8.2
 */
public interface MessageDispatcher {

    /**
     * Dispatches a message event to the specified set of receivers asynchronously.
     * Each receiver receives an individually created message event for personalized message processing.
     * Dispatching is skipped if the receiver set is empty.
     *
     * @param eventMetadata Metadata containing event information for message creation
     * @param receivers The pre-filtered set of players to receive the message
     * @return The set of players who received the message (same as input receivers)
     */
    Set<FPlayer> dispatch(@NonNull EventMetadata eventMetadata, @NonNull Set<FPlayer> receivers);

    /**
     * Directly dispatches a pre-built message send event through the event system.
     *
     * @param messageSendEvent The message send event to dispatch
     * @return The dispatched message send event
     */
    MessageSendEvent dispatch(MessageSendEvent messageSendEvent);

    /**
     * Dispatches a message event to eligible receivers determined by the event metadata.
     * Automatically resolves the receiver list using {@link #createReceivers(EventMetadata)}.
     *
     * @param eventMetadata Metadata containing event information and filtering criteria
     * @return The set of players who received the message
     */
    default Set<FPlayer> dispatch(@NonNull EventMetadata eventMetadata) {
        return dispatch(eventMetadata, createReceivers(eventMetadata));
    }

    /**
     * Builds the message, works out who receives it and sends it.
     *
     * @param module the module the message came from
     * @param eventMetadata how it should be delivered
     * @return the players it reached
     * @deprecated since 1.14.1, use {@link #dispatch(EventMetadata)}
     */
    @Deprecated(since = "1.14.1")
    default Set<FPlayer> dispatch(@NonNull ModuleLocalization module, @NonNull EventMetadata eventMetadata) {
        return dispatch(eventMetadata.withModule(module.name()));
    }

    /**
     * Dispatches a message event to eligible receivers determined by the module name and event metadata.
     * Automatically resolves the receiver list using {@link #createReceivers(EventMetadata)}.
     *
     * @param moduleName The module name used for localization and receiver filtering
     * @param eventMetadata Metadata containing event information and filtering criteria
     * @return The set of players who received the message
     * @deprecated since 1.14.1, use {@link #dispatch(EventMetadata)}
     */
    @Deprecated(since = "1.14.1")
    default Set<FPlayer> dispatch(@NonNull ModuleName moduleName, @NonNull EventMetadata eventMetadata) {
        return dispatch(eventMetadata.withModule(moduleName));
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
     * @deprecated since 1.14.1, use {@link #dispatch(EventMetadata, Set)}
     */
    @Deprecated(since = "1.14.1")
    default Set<FPlayer> dispatch(@NonNull ModuleName moduleName, @NonNull EventMetadata eventMetadata, @NonNull Set<FPlayer> receivers) {
        return dispatch(eventMetadata.withModule(moduleName), receivers);
    }

    /**
     * Creates and populates the receiver list for the message event based on filtering criteria.
     * Applies player filters, range filters, and module settings checks to determine eligible receivers.
     *
     * @param eventMetadata Metadata containing event information and filtering criteria
     * @return A set of eligible players to receive the message, or an empty set if the event is for proxy and cancelled
     */
    Set<FPlayer> createReceivers(EventMetadata eventMetadata);

    /**
     * Creates and populates the receiver list for the message event based on filtering criteria.
     * Applies player filters, range filters, and module settings checks to determine eligible receivers.
     *
     * @param moduleName The module name containing localization and settings to check for each receiver
     * @param eventMetadata Metadata containing event information and filtering criteria
     * @return A set of eligible players to receive the message, or an empty set if the event is for proxy and cancelled
     * @deprecated since 1.14.1, use {@link #createReceivers(EventMetadata)}
     */
    @Deprecated(since = "1.14.1")
    default Set<FPlayer> createReceivers(ModuleName moduleName, EventMetadata eventMetadata) {
        return createReceivers(eventMetadata.withModule(moduleName));
    }

    /**
     * Creates a complete message send event for a specific receiver.
     * Builds formatted message components including main message, format wrapper, and destination subtext.
     *
     * @param fReceiver The player receiving the message
     * @param eventMetadata Metadata containing event information and message content
     * @return A fully constructed MessageSendEvent ready for dispatch
     */
    MessageSendEvent createMessageEvent(FPlayer fReceiver, EventMetadata eventMetadata);

    /**
     * Creates a complete message send event for a specific receiver.
     * Builds formatted message components including main message, format wrapper, and destination subtext.
     *
     * @param fReceiver The player receiving the message
     * @param moduleName The name identifier for the module
     * @param eventMetadata Metadata containing event information and message content
     * @return A fully constructed MessageSendEvent ready for dispatch
     * @deprecated since 1.14.1, use {@link #createMessageEvent(FPlayer, EventMetadata)}
     */
    @Deprecated(since = "1.14.1")
    default MessageSendEvent createMessageEvent(FPlayer fReceiver, ModuleName moduleName, EventMetadata eventMetadata) {
        return createMessageEvent(fReceiver, eventMetadata.withModule(moduleName));
    }

}