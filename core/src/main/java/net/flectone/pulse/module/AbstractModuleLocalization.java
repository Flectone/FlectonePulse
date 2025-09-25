package net.flectone.pulse.module;

import com.google.inject.Inject;
import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.event.message.PreMessageSendEvent;
import net.flectone.pulse.model.util.Cooldown;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.platform.filter.RangeFilter;
import net.flectone.pulse.platform.sender.CooldownSender;
import net.flectone.pulse.platform.sender.DisableSender;
import net.flectone.pulse.platform.sender.MuteSender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static net.flectone.pulse.execution.pipeline.MessagePipeline.ReplacementTag.empty;

public abstract class AbstractModuleLocalization<M extends Localization.Localizable> extends AbstractModule {

    @Getter private final MessageType messageType;

    @Inject private FPlayerService fPlayerService;
    @Inject private CooldownSender cooldownSender;
    @Inject private DisableSender disableSender;
    @Inject private MuteSender muteSender;
    @Inject private FileResolver fileResolver;
    @Inject private RangeFilter rangeFilter;
    @Inject private MessagePipeline messagePipeline;
    @Inject private EventDispatcher eventDispatcher;

    @Getter private Cooldown moduleCooldown;
    @Getter private Sound moduleSound;

    protected AbstractModuleLocalization(MessageType messageType) {
        this.messageType = messageType;
    }

    public abstract M localization(FEntity sender);

    public M localization() {
        return localization(FPlayer.UNKNOWN);
    }

    @Override
    protected void addDefaultPredicates() {
        super.addDefaultPredicates();

        addPredicate((fPlayer, needBoolean) -> needBoolean && disableSender.sendIfDisabled(fPlayer, fPlayer, messageType));
        addPredicate((fPlayer, needBoolean) -> needBoolean && cooldownSender.sendIfCooldown(fPlayer, getModuleCooldown()));
        addPredicate((fPlayer, needBoolean) -> needBoolean && muteSender.sendIfMuted(fPlayer));
    }

    public Sound createSound(Sound sound, Permission.IPermission permission) {
        this.moduleSound = sound;

        if (permission != null) {
            registerPermission(permission);
            sound.setPermission(permission.getName());
        }

        return sound;
    }

    public Cooldown createCooldown(Cooldown cooldown, Permission.IPermission permission) {
        this.moduleCooldown = cooldown;

        if (permission != null) {
            registerPermission(permission);
            cooldown.setPermissionBypass(permission.getName());
        }

        return this.moduleCooldown;
    }

    @SuppressWarnings("unchecked")
    public EventMetadata.EventMetadataBuilder<M, ?, ?> metadataBuilder() {
        return (EventMetadata.EventMetadataBuilder<M, ?, ?>) EventMetadata.builder();
    }

    public List<FPlayer> createReceivers(MessageType messageType, EventMetadata<M> eventMetadata) {
        String rawFormat = eventMetadata.resolveFormat(FPlayer.UNKNOWN, localization());
        PreMessageSendEvent preMessageSendEvent = new PreMessageSendEvent(messageType, rawFormat, eventMetadata);

        eventDispatcher.dispatch(preMessageSendEvent);

        if (preMessageSendEvent.isCancelled()) return Collections.emptyList();

        FPlayer filterPlayer = eventMetadata.getFilterPlayer();

        return fPlayerService.getFPlayersWithConsole().stream()
                .filter(eventMetadata.getFilter())
                .filter(rangeFilter.createFilter(filterPlayer, eventMetadata.getRange()))
                .filter(fReceiver -> fReceiver.isSetting(messageType))
                .toList();
    }

    public void sendMessage(EventMetadata<M> eventMetadata) {
        sendMessage(this.messageType, eventMetadata);
    }

    public void sendMessage(MessageType messageType, EventMetadata<M> eventMetadata) {
        List<FPlayer> receivers = createReceivers(messageType, eventMetadata);
        sendMessage(messageType, receivers, eventMetadata);
    }

    public void sendMessage(List<FPlayer> receivers, EventMetadata<M> eventMetadata) {
        sendMessage(this.messageType, receivers, eventMetadata);
    }

    @Async
    public void sendMessage(MessageType messageType, List<FPlayer> receivers, EventMetadata<M> eventMetadata) {
        if (receivers.isEmpty()) return;

        receivers.forEach(receiver -> {
            // example
            // format: TheFaser > <message>
            // message: hello world!
            // final formatted message: TheFaser > hello world!
            Component messageComponent = buildMessageComponent(receiver, eventMetadata);
            Component formatComponent = buildFormatComponent(receiver, eventMetadata, messageComponent);

            // destination subtext
            Component subComponent = Component.empty();
            Destination destination = eventMetadata.getDestination();
            if (destination.getType() == Destination.Type.TITLE
                    || destination.getType() == Destination.Type.SUBTITLE) {
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
    }

    public void sendErrorMessage(EventMetadata<M> eventMetadata) {
        sendMessage(MessageType.ERROR, eventMetadata);
    }

    private Component buildSubcomponent(FPlayer receiver, EventMetadata<M> eventMetadata, Component message) {
        Destination destination = eventMetadata.getDestination();
        return destination.getSubtext().isEmpty()
                ? Component.empty()
                : messagePipeline.builder(eventMetadata.getSender(), receiver, destination.getSubtext())
                .flag(MessageFlag.SENDER_COLOR_OUT, eventMetadata.isSenderColorOut())
                .tagResolvers(messageTag(message))
                .build();
    }

    private Component buildMessageComponent(FPlayer receiver, EventMetadata<M> eventMetadata) {
        String message = eventMetadata.getMessage();
        if (StringUtils.isEmpty(message)) return Component.empty();

        FEntity sender = eventMetadata.getSender();
        boolean senderColorOut = eventMetadata.isSenderColorOut();

        MessagePipeline.Builder messageBuilder = messagePipeline.builder(sender, receiver, message)
                .flag(MessageFlag.USER_MESSAGE, true)
                .flag(MessageFlag.SENDER_COLOR_OUT, senderColorOut)
                .flag(MessageFlag.MENTION, !receiver.isUnknown());

        return messageBuilder.build();
    }

    private Component buildFormatComponent(FPlayer receiver, EventMetadata<M> eventMetadata, Component message) {
        String formatContent = eventMetadata.resolveFormat(receiver, localization(receiver));
        if (StringUtils.isEmpty(formatContent)) return Component.empty();

        FEntity sender = eventMetadata.getSender();
        boolean senderColorOut = eventMetadata.isSenderColorOut();

        MessagePipeline.Builder formatBuilder = messagePipeline
                .builder(eventMetadata.getUuid(), sender, receiver, formatContent)
                .flag(MessageFlag.SENDER_COLOR_OUT, senderColorOut)
                .tagResolvers(eventMetadata.getTagResolvers(receiver))
                .tagResolvers(messageTag(message));

        if (!receiver.isUnknown()) {
            formatBuilder = formatBuilder
                    .setUserMessage(eventMetadata.getMessage())
                    .translate(formatContent.contains("<translate")); // support new <translate> and old <translateto>
        }

        return formatBuilder.build();
    }

    public TagResolver messageTag(Component message) {
        return TagResolver.resolver("message", (argumentQueue, context) -> Tag.inserting(message));
    }

    public TagResolver targetTag(@TagPattern String tag, FPlayer receiver, @Nullable FEntity target) {
        if (!isEnable() || target == null) return empty(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            Component component = messagePipeline.builder(target, receiver, "<display_name>").build();

            return Tag.selfClosingInserting(component);
        });
    }

    public TagResolver targetTag(FPlayer receiver, @Nullable FEntity target) {
        return targetTag("target", receiver, target);
    }
}
