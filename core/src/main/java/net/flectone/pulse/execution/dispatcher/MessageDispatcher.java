package net.flectone.pulse.execution.dispatcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.MessagePrepareEvent;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.platform.filter.RangeFilter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessageDispatcher {

    private final FPlayerService fPlayerService;
    private final RangeFilter rangeFilter;
    private final MessagePipeline messagePipeline;
    private final EventDispatcher eventDispatcher;
    private final TaskScheduler taskScheduler;

    public <L extends LocalizationSetting> List<FPlayer> createReceivers(ModuleLocalization<L> module,
                                                                         EventMetadata<L> eventMetadata) {
        return createReceivers(module.name(), module, eventMetadata);
    }

    public <L extends LocalizationSetting> List<FPlayer> createReceivers(ModuleName moduleName,
                                                                         ModuleLocalization<L> module,
                                                                         EventMetadata<L> eventMetadata) {
        String rawFormat = eventMetadata.resolveFormat(FPlayer.UNKNOWN, module.localization());

        MessagePrepareEvent messagePrepareEvent = eventDispatcher.dispatch(new MessagePrepareEvent(moduleName, rawFormat, eventMetadata));

        // if canceled, it means that message was sent to Proxy
        if (messagePrepareEvent.cancelled()) return Collections.emptyList();

        return fPlayerService.getFPlayersWithConsole().stream()
                .filter(eventMetadata.filter())
                .filter(rangeFilter.createFilter(eventMetadata.filterPlayer(), eventMetadata.range()))
                .filter(fReceiver -> fReceiver.isSetting(moduleName))
                .toList();
    }

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

    public <L extends LocalizationSetting> void dispatch(ModuleLocalization<L> module,
                                                         EventMetadata<L> eventMetadata) {
        dispatch(module.name(), module, eventMetadata);
    }

    public <L extends LocalizationSetting> void dispatch(ModuleName moduleName,
                                                         ModuleLocalization<L> module,
                                                         EventMetadata<L> eventMetadata) {
        List<FPlayer> receivers = createReceivers(moduleName, module, eventMetadata);
        dispatch(moduleName, receivers, module, eventMetadata);
    }

    public <L extends LocalizationSetting> void dispatch(List<FPlayer> receivers,
                                                         ModuleLocalization<L> module,
                                                         EventMetadata<L> eventMetadata) {
        dispatch(module.name(), receivers, module, eventMetadata);
    }

    public <L extends LocalizationSetting> void dispatch(ModuleName moduleName,
                                                         List<FPlayer> receivers,
                                                         ModuleLocalization<L> module,
                                                         EventMetadata<L> eventMetadata) {
        if (receivers.isEmpty()) return;

        // fix Folia issue
        FPlayer regionPlayer = eventMetadata.sender() instanceof FPlayer fPlayer
                ? fPlayer
                : fPlayerService.getRandomFPlayer();

        taskScheduler.runRegion(regionPlayer, () -> receivers.forEach(fReceiver ->
                dispatch(createMessageEvent(fReceiver, moduleName, module, eventMetadata)))
        );
    }

    public MessageSendEvent dispatch(MessageSendEvent messageSendEvent) {
        return eventDispatcher.dispatch(messageSendEvent);
    }

    public <L extends LocalizationSetting> void dispatchError(ModuleLocalization<L> module, EventMetadata<L> eventMetadata) {
        dispatch(ModuleName.ERROR, module, eventMetadata);
    }

    private <L extends LocalizationSetting> Component buildSubcomponent(FPlayer receiver,
                                                                        EventMetadata<L> eventMetadata,
                                                                        Component message) {
        Destination destination = eventMetadata.destination();
        if (destination.subtext().isEmpty()) return Component.empty();

        MessageContext context = messagePipeline.createContext(eventMetadata.sender(), receiver, destination.subtext())
                .withFlags(eventMetadata.flags())
                .addTagResolver(messagePipeline.messageTag(message));

        return messagePipeline.build(context);
    }

    private <L extends LocalizationSetting> Component buildMessageComponent(FPlayer receiver,
                                                                            EventMetadata<L> eventMetadata) {
        String message = eventMetadata.message();
        if (StringUtils.isEmpty(message)) return Component.empty();

        MessageContext context = messagePipeline.createContext(eventMetadata.sender(), receiver, message)
                .withFlags(eventMetadata.flags())
                .addFlag(MessageFlag.PLAYER_MESSAGE, true);

        return messagePipeline.build(context);
    }

    private <L extends LocalizationSetting> Component buildFormatComponent(FPlayer receiver,
                                                                           EventMetadata<L> eventMetadata,
                                                                           ModuleLocalization<L> module,
                                                                           Component message) {
        String formatContent = eventMetadata.resolveFormat(receiver, module.localization(receiver));
        if (StringUtils.isEmpty(formatContent)) return Component.empty();

        FEntity sender = eventMetadata.sender();

        MessageContext messageContext = messagePipeline.createContext(eventMetadata.uuid(), sender, receiver, formatContent)
                .withFlags(eventMetadata.flags())
                .addTagResolvers(eventMetadata.resolveTags(receiver))
                .addTagResolver(messagePipeline.messageTag(message));

        if (!receiver.isUnknown()) {
            messageContext = messageContext
                    .withUserMessage(eventMetadata.message());
        }

        return messagePipeline.build(messageContext);
    }

}
