package net.flectone.pulse.module;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import net.flectone.pulse.config.setting.*;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.SchedulerRunnable;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.MessagePrepareEvent;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.util.Cooldown;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.module.message.quit.model.QuitMetadata;
import net.flectone.pulse.platform.filter.RangeFilter;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.type.tuple.Pair;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static net.flectone.pulse.execution.pipeline.MessagePipeline.ReplacementTag.empty;

public abstract class AbstractModuleLocalization<M extends LocalizationSetting> extends AbstractModule {

    @Inject private FPlayerService fPlayerService;
    @Inject private RangeFilter rangeFilter;
    @Inject private MessagePipeline messagePipeline;
    @Inject private EventDispatcher eventDispatcher;
    @Inject private TaskScheduler taskScheduler;

    public abstract MessageType messageType();

    public abstract M localization(FEntity sender);

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        ImmutableList.Builder<PermissionSetting> builder = super.permissionBuilder();

        if (permission() instanceof CooldownPermissionSetting cooldownPermission) {
            builder.add(cooldownPermission.cooldownBypass());
        }

        if (permission() instanceof SoundPermissionSetting soundPermission) {
            builder.add(soundPermission.sound());
        }

        return builder;
    }

    public M localization() {
        return localization(FPlayer.UNKNOWN);
    }

    public Optional<Pair<Cooldown, PermissionSetting>> cooldown() {
        if (config() instanceof CooldownConfigSetting cooldownSetting
                && permission() instanceof CooldownPermissionSetting cooldownPermission) {
            return Optional.of(Pair.of(cooldownSetting.cooldown(), cooldownPermission.cooldownBypass()));
        }

        return Optional.empty();
    }

    public Pair<Cooldown, PermissionSetting> cooldownOrThrow() {
        return cooldown().orElseThrow(() -> new IllegalStateException(
                "Cooldown not configured for module: " + getClass().getSimpleName()
        ));
    }

    public Optional<Pair<Sound, PermissionSetting>> sound() {
        if (config() instanceof SoundConfigSetting soundSetting
                && permission() instanceof SoundPermissionSetting soundPermission) {
            return Optional.of(Pair.of(soundSetting.sound(), soundPermission.sound()));
        }

        return Optional.empty();
    }

    public Pair<Sound, PermissionSetting> soundOrThrow() {
        return sound().orElseThrow(() -> new IllegalStateException(
                "Sound not configured for module: " + getClass().getSimpleName()
        ));
    }

    public List<FPlayer> createReceivers(MessageType messageType, EventMetadata<M> eventMetadata) {
        String rawFormat = eventMetadata.resolveFormat(FPlayer.UNKNOWN, localization());

        MessagePrepareEvent messagePrepareEvent = eventDispatcher.dispatch(new MessagePrepareEvent(messageType, rawFormat, eventMetadata));

        // if canceled, it means that message was sent to Proxy
        if (messagePrepareEvent.cancelled()) return Collections.emptyList();

        return fPlayerService.getFPlayersWithConsole().stream()
                .filter(eventMetadata.filter())
                .filter(rangeFilter.createFilter(eventMetadata.filterPlayer(), eventMetadata.range()))
                .filter(fReceiver -> fReceiver.isSetting(messageType))
                .toList();
    }

    public void sendMessage(EventMetadata<M> eventMetadata) {
        sendMessage(messageType(), eventMetadata);
    }

    public void sendMessage(MessageType messageType, EventMetadata<M> eventMetadata) {
        List<FPlayer> receivers = createReceivers(messageType, eventMetadata);
        sendMessage(messageType, receivers, eventMetadata);
    }

    public void sendMessage(List<FPlayer> receivers, EventMetadata<M> eventMetadata) {
        sendMessage(messageType(), receivers, eventMetadata);
    }

    public void sendMessage(MessageType messageType, List<FPlayer> receivers, EventMetadata<M> eventMetadata) {
        if (receivers.isEmpty()) return;

        SchedulerRunnable sendMessageRunnable = () -> receivers.forEach(receiver -> {
            // example
            // format: TheFaser > <message>
            // message: hello world!
            // final formatted message: TheFaser > hello world!
            Component messageComponent = buildMessageComponent(receiver, eventMetadata);
            Component formatComponent = buildFormatComponent(receiver, eventMetadata, messageComponent);

            // destination subtext
            Component subComponent = Component.empty();
            Destination destination = eventMetadata.destination();
            if (StringUtils.isNotEmpty(destination.subtext())) {
                subComponent = buildSubcomponent(receiver, eventMetadata, messageComponent);
            }

            eventDispatcher.dispatch(new MessageSendEvent(
                    messageType,
                    receiver,
                    formatComponent,
                    subComponent,
                    eventMetadata
            ));
        });

        // fix Folia issue
        if (eventMetadata instanceof QuitMetadata<M>) {
            taskScheduler.runAsync(sendMessageRunnable);
        } else {
            FPlayer regionPlayer = eventMetadata.sender() instanceof FPlayer fPlayer
                    ? fPlayer
                    : fPlayerService.getRandomFPlayer();

            taskScheduler.runRegion(regionPlayer, sendMessageRunnable);
        }
    }

    public void sendErrorMessage(EventMetadata<M> eventMetadata) {
        sendMessage(MessageType.ERROR, eventMetadata);
    }

    private Component buildSubcomponent(FPlayer receiver, EventMetadata<M> eventMetadata, Component message) {
        Destination destination = eventMetadata.destination();
        if (destination.subtext().isEmpty()) return Component.empty();

        MessageContext context = messagePipeline.createContext(eventMetadata.sender(), receiver, destination.subtext())
                .withFlags(eventMetadata.flags())
                .addTagResolver(messageTag(message));

        return messagePipeline.build(context);
    }

    private Component buildMessageComponent(FPlayer receiver, EventMetadata<M> eventMetadata) {
        String message = eventMetadata.message();
        if (StringUtils.isEmpty(message)) return Component.empty();

        MessageContext context = messagePipeline.createContext(eventMetadata.sender(), receiver, message)
                .withFlags(eventMetadata.flags())
                .addFlag(MessageFlag.USER_MESSAGE, true);

        return messagePipeline.build(context);
    }

    private Component buildFormatComponent(FPlayer receiver, EventMetadata<M> eventMetadata, Component message) {
        String formatContent = eventMetadata.resolveFormat(receiver, localization(receiver));
        if (StringUtils.isEmpty(formatContent)) return Component.empty();

        FEntity sender = eventMetadata.sender();

        MessageContext messageContext = messagePipeline.createContext(eventMetadata.uuid(), sender, receiver, formatContent)
                .withFlags(eventMetadata.flags())
                .addTagResolvers(eventMetadata.resolveTags(receiver))
                .addTagResolver(messageTag(message));

        if (!receiver.isUnknown()) {
            messageContext = messageContext
                    .withUserMessage(eventMetadata.message())
                    .addFlag(MessageFlag.TRANSLATE, formatContent.contains("<translate"));
        }

        return messagePipeline.build(messageContext);
    }

    public TagResolver messageTag(Component message) {
        return TagResolver.resolver("message", (argumentQueue, context) -> Tag.inserting(message));
    }

    public TagResolver targetTag(@TagPattern String tag, String formatTarget, FPlayer receiver, @Nullable FEntity target) {
        if (!isEnable() || target == null) return empty(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            int targetIndex = 0;
            if (argumentQueue.hasNext()) {
                targetIndex = argumentQueue.pop().asInt().orElse(0);
            }
            
            MessageContext messageContext = messagePipeline.createContext(target, receiver,
                    Strings.CS.replace(formatTarget, "<index>", String.valueOf(targetIndex))
            );

            return Tag.selfClosingInserting(messagePipeline.build(messageContext));
        });
    }

    public TagResolver targetTag(@TagPattern String tag, FPlayer receiver, @Nullable FEntity target) {
        return targetTag(tag, "<display_name:<index>>", receiver, target);
    }

    public TagResolver targetTag(FPlayer receiver, @Nullable FEntity target) {
        return targetTag("target", receiver, target);
    }
}
