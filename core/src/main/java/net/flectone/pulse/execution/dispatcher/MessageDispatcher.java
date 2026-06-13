package net.flectone.pulse.execution.dispatcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.setting.LocalizationSetting;
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
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;


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
     * Dispatches an error message using the specified module localization.
     *
     * @param module The module containing localization settings
     * @param eventMetadata Metadata containing event information and context
     * @param <L> Type of localization setting
     * @return Event metadata with receiver list
     */
    public <L extends LocalizationSetting, E extends EventMetadata<L>> E dispatchError(ModuleLocalization<L> module,
                                                                                       E eventMetadata) {
        return dispatch(ModuleName.ERROR, module, eventMetadata);
    }

    /**
     * Dispatches a message using the module's name as the identifier.
     *
     * @param module The module containing localization settings
     * @param eventMetadata Metadata containing event information and context
     * @param <L> Type of localization setting
     * @return Event metadata with receiver list
     */
    public <L extends LocalizationSetting, E extends EventMetadata<L>> E dispatch(ModuleLocalization<L> module,
                                                                                  E eventMetadata) {
        return dispatch(module.name(), module, eventMetadata);
    }

    /**
     * Dispatches a message to all eligible receivers based on the provided metadata.
     * Creates receiver list and routes the message through the dispatch pipeline.
     *
     * @param moduleName The name identifier for the module
     * @param module The module containing localization settings
     * @param eventMetadata Metadata containing event information and context
     * @param <L> Type of localization setting
     * @return Event metadata with receiver list
     */
    public <L extends LocalizationSetting, E extends EventMetadata<L>> E dispatch(ModuleName moduleName,
                                                                                  ModuleLocalization<L> module,
                                                                                  E eventMetadata) {
        return dispatchForReceivers(moduleName, module, createReceivers(moduleName, module, eventMetadata));
    }

    /**
     * Dispatches a message to pre-configured receivers with Folia region-aware scheduling.
     * Ensures thread-safe execution by running in the appropriate region context.
     *
     * @param moduleName The name identifier for the module
     * @param module The module containing localization settings
     * @param eventMetadata Metadata containing event information and pre-configured receivers
     * @param <L> Type of localization setting
     * @return Event metadata with receiver list
     */
    public <L extends LocalizationSetting, E extends EventMetadata<L>> E dispatchForReceivers(ModuleName moduleName,
                                                                                              ModuleLocalization<L> module,
                                                                                              E eventMetadata) {
        if (!eventMetadata.receivers().isEmpty()) {
            taskScheduler.runAsync(() -> eventMetadata.receivers().forEach(fReceiver ->
                    dispatch(createMessageEvent(fReceiver, moduleName, module, eventMetadata)))
            );
        }

        return eventMetadata;
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
     * @param module The module containing localization settings
     * @param eventMetadata Metadata containing event information and filtering criteria
     * @param <L> Type of localization setting
     * @param <E> Type of event metadata extending EventMetadata
     * @return Event metadata with populated receiver list, or original metadata if canceled
     */
    public <L extends LocalizationSetting, E extends EventMetadata<L>> E createReceivers(ModuleLocalization<L> module,
                                                                                         E eventMetadata) {
        return createReceivers(module.name(), module, eventMetadata);
    }

    /**
     * Creates and populates the receiver list with module name specification.
     * Filters players based on multiple criteria including range, settings, and custom filters.
     * Triggers a MessagePrepareEvent that may cancel the message if sent to Proxy.
     *
     * @param moduleName The name identifier for the module
     * @param module The module containing localization settings
     * @param eventMetadata Metadata containing event information and filtering criteria
     * @param <L> Type of localization setting
     * @param <E> Type of event metadata extending EventMetadata
     * @return Event metadata with populated receiver list, or original metadata if canceled
     */
    @SuppressWarnings("unchecked")
    public <L extends LocalizationSetting, E extends EventMetadata<L>> E createReceivers(ModuleName moduleName,
                                                                                         ModuleLocalization<L> module,
                                                                                         E eventMetadata) {
        String rawFormat = eventMetadata.resolveFormat(FPlayer.UNKNOWN, module.localization());

        MessagePrepareEvent messagePrepareEvent = eventDispatcher.dispatch(new MessagePrepareEvent(moduleName, rawFormat, eventMetadata));

        // if canceled, it means that message was sent to Proxy
        if (messagePrepareEvent.isForProxy() && messagePrepareEvent.cancelled()) return eventMetadata;

        EventMetadata<L> newEventMetadata = (EventMetadata<L>) messagePrepareEvent.eventMetadata();

        return (E) newEventMetadata.withBase(newEventMetadata.base().withReceivers(fPlayerService.getFPlayersWithConsole().stream()
                        .filter(rangeFilter.createFilter(newEventMetadata))
                        .filter(fReceiver -> socialService.isSetting(fReceiver, moduleName))
                        .toList()
                )
        );
    }

    /**
     * Creates a complete message send event for a specific receiver.
     * Builds formatted message components including main message, format wrapper, and destination subtext.
     *
     * @param fReceiver The player receiving the message
     * @param moduleName The name identifier for the module
     * @param module The module containing localization settings
     * @param eventMetadata Metadata containing event information and message content
     * @param <L> Type of localization setting
     * @return A fully constructed MessageSendEvent ready for dispatch
     */
    public <L extends LocalizationSetting> MessageSendEvent createMessageEvent(FPlayer fReceiver,
                                                                               ModuleName moduleName,
                                                                               ModuleLocalization<L> module,
                                                                               EventMetadata<L> eventMetadata) {
        // example
        // format: TheFaser > <message>
        // message: hello world!
        // final formatted message: TheFaser > hello world!
        Component messageComponent = buildMessageComponent(fReceiver, eventMetadata);
        Component formatComponent = buildFormatComponent(fReceiver, eventMetadata, module, messageComponent);

        // destination subtext
        Component subComponent = Component.empty();
        Destination destination = eventMetadata.destination();
        if (StringUtils.isNotEmpty(destination.subtext())) {
            subComponent = buildSubcomponent(fReceiver, eventMetadata, messageComponent);
        }

        return new MessageSendEvent(
                moduleName,
                fReceiver,
                formatComponent,
                subComponent,
                eventMetadata
        );
    }

    private <L extends LocalizationSetting> Component buildSubcomponent(FPlayer receiver,
                                                                        EventMetadata<L> eventMetadata,
                                                                        Component message) {
        Destination destination = eventMetadata.destination();
        if (destination.subtext().isEmpty()) return Component.empty();

        return messagePipeline.build(MessageContext.builder()
                .sender(eventMetadata.sender())
                .receiver(receiver)
                .message(destination.subtext())
                .flags(eventMetadata.flags())
                .tagResolver(messagePipeline.messageTag(message))
                .build()
        );
    }

    private <L extends LocalizationSetting> Component buildMessageComponent(FPlayer receiver,
                                                                            EventMetadata<L> eventMetadata) {
        String message = eventMetadata.message();
        if (StringUtils.isEmpty(message)) return Component.empty();

        return messagePipeline.build(MessageContext.builder()
                .sender(eventMetadata.sender())
                .receiver(receiver)
                .message(message)
                .flags(eventMetadata.flags())
                .flag(MessageFlag.PLAYER_MESSAGE, true)
                .build()
        );
    }

    private <L extends LocalizationSetting> Component buildFormatComponent(FPlayer receiver,
                                                                           EventMetadata<L> eventMetadata,
                                                                           ModuleLocalization<L> module,
                                                                           Component message) {
        String formatContent = eventMetadata.resolveFormat(receiver, module.localization(receiver));
        if (StringUtils.isEmpty(formatContent)) return Component.empty();

        MessageContext.MessageContextBuilder messageContextBuilder = MessageContext.builder()
                .messageUUID(eventMetadata.uuid())
                .sender(eventMetadata.sender())
                .receiver(receiver)
                .message(formatContent)
                .userMessage(eventMetadata.message())
                .tagResolver(messagePipeline.messageTag(message))
                .flags(eventMetadata.flags());

        TagResolver[] tagResolvers = eventMetadata.resolveTags(receiver);
        if (tagResolvers != null) {
            messageContextBuilder = messageContextBuilder.tagResolvers(tagResolvers);
        }

        return messagePipeline.build(messageContextBuilder.build());
    }

}
