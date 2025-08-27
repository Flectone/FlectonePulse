package net.flectone.pulse.module;

import com.google.inject.Inject;
import lombok.Getter;
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
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.MuteChecker;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.DisableSource;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractModuleLocalization<M extends Localization.Localizable> extends AbstractModule {

    private final Function<Localization, M> localizationFunction;
    private final MessageType messageType;

    @Inject private FPlayerService fPlayerService;
    @Inject private PlatformPlayerAdapter platformPlayerAdapter;
    @Inject private PermissionChecker permissionChecker;
    @Inject private MuteChecker muteChecker;
    @Inject private FileResolver fileResolver;
    @Inject private MessagePipeline messagePipeline;
    @Inject private ModerationMessageFormatter moderationMessageFormatter;
    @Inject private TimeFormatter timeFormatter;
    @Inject private EventDispatcher eventDispatcher;

    @Getter private Cooldown moduleCooldown;
    @Getter private Sound moduleSound;

    protected AbstractModuleLocalization(Function<Localization, M> localizationMFunction, MessageType messageType) {
        this.localizationFunction = localizationMFunction;
        this.messageType = messageType;
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

    public String getCooldownMessage(FEntity sender) {
        return fileResolver.getLocalization(sender).getCooldown();
    }

    public M resolveLocalization() {
        return localizationFunction.apply(fileResolver.getLocalization());
    }

    public M resolveLocalization(FEntity sender) {
        return localizationFunction.apply(fileResolver.getLocalization(sender));
    }

    public boolean checkCooldown(FEntity entity) {
        if (getModuleCooldown() == null) return false;
        if (!getModuleCooldown().isEnable()) return false;
        if (!(entity instanceof FPlayer fPlayer)) return false;
        if (permissionChecker.check(fPlayer, getModuleCooldown().getPermissionBypass())) return false;
        if (!getModuleCooldown().isCooldown(fPlayer.getUuid())) return false;

        long timeLeft = getModuleCooldown().getTimeLeft(fPlayer);

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .format(timeFormatter.format(fPlayer, timeLeft, getCooldownMessage(fPlayer)))
                .build()
        );

        return true;
    }

    public boolean checkMute(@NotNull FEntity entity) {
        if (!(entity instanceof FPlayer fPlayer)) return false;

        MuteChecker.Status status = muteChecker.check(fPlayer);
        if (status == MuteChecker.Status.NONE) return false;

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .format(moderationMessageFormatter.buildMuteMessage(fPlayer, status))
                .build()
        );

        return true;
    }

    public boolean sendDisableMessage(FEntity fPlayer, DisableSource whoDisableCommand) {
        return sendDisableMessage(fPlayer, fPlayer, whoDisableCommand);
    }

    public boolean sendDisableMessage(FEntity fPlayer, @NotNull FEntity fReceiver, DisableSource action) {
        Localization.Command.Chatsetting.Disable localization = fileResolver.getLocalization(fReceiver).getCommand().getChatsetting().getDisable();

        String format = switch (action) {
            case HE -> localization.getHe();
            case YOU -> localization.getYou();
            case SERVER -> localization.getServer();
        };

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .format(format)
                .build()
        );

        return true;
    }

    public boolean checkIgnore(FPlayer fSender, FPlayer fReceiver) {
        Localization.Command.Ignore localization = fileResolver.getLocalization(fSender).getCommand().getIgnore();

        if (fSender.isIgnored(fReceiver)) {
            sendMessage(metadataBuilder()
                    .sender(fSender)
                    .format(localization.getYou())
                    .build()
            );

            return true;
        }

        if (fReceiver.isIgnored(fSender)) {
            sendMessage(metadataBuilder()
                    .sender(fSender)
                    .format(localization.getHe())
                    .build()
            );

            return true;
        }

        return false;
    }

    public Predicate<FPlayer> rangeFilter(FEntity sender, Range range) {
        if (range.is(Range.Type.PLAYER)) {
            return sender::equals;
        }

        if (!(sender instanceof FPlayer fPlayer) || fPlayer.isUnknown()) {
            return player -> true;
        }

        return createRangePredicate(fPlayer, range);
    }

    private Predicate<FPlayer> createRangePredicate(FPlayer fPlayer, Range range) {
        return fReceiver -> {
            if (fReceiver.isUnknown()) return true;
            if (fReceiver.isIgnored(fPlayer)) return false;

            return switch (range.getType()) {
                case BLOCKS -> checkDistance(fPlayer, fReceiver, range.getValue());
                case WORLD_NAME -> checkWorldNamePermission(fPlayer, fReceiver);
                case WORLD_TYPE -> checkWorldTypePermission(fPlayer, fReceiver);
                default -> true;
            };
        };
    }

    private boolean checkDistance(FPlayer fPlayer, FPlayer fReceiver, int range) {
        double distance = platformPlayerAdapter.distance(fPlayer, fReceiver);
        return distance != -1.0 && distance <= range;
    }

    private boolean checkWorldNamePermission(FPlayer fPlayer, FPlayer fReceiver) {
        String worldName = platformPlayerAdapter.getWorldName(fPlayer);
        if (worldName.isEmpty()) return true;
        return permissionChecker.check(fReceiver, "flectonepulse.world.name." + worldName);
    }

    private boolean checkWorldTypePermission(FPlayer fPlayer, FPlayer fReceiver) {
        String worldType = platformPlayerAdapter.getWorldEnvironment(fPlayer);
        if (worldType.isEmpty()) return true;
        return permissionChecker.check(fReceiver, "flectonepulse.world.type." + worldType);
    }

    @SuppressWarnings("unchecked")
    public EventMetadata.EventMetadataBuilder<M, ?, ?> metadataBuilder() {
        return (EventMetadata.EventMetadataBuilder<M, ?, ?>) EventMetadata.builder();
    }

    public List<FPlayer> createReceivers(EventMetadata<M> eventMetadata) {
        String rawFormat = eventMetadata.resolveFormat(FPlayer.UNKNOWN, resolveLocalization());
        PreMessageSendEvent preMessageSendEvent = new PreMessageSendEvent(messageType, rawFormat, eventMetadata);

        eventDispatcher.dispatch(preMessageSendEvent);

        if (preMessageSendEvent.isCancelled()) return Collections.emptyList();

        return fPlayerService.getFPlayersWithConsole().stream()
                .filter(eventMetadata.getFilter())
                .filter(rangeFilter(eventMetadata.getReceiver(), eventMetadata.getRange()))
                .toList();
    }

    public void sendMessage(EventMetadata<M> eventMetadata) {
        sendMessage(this.messageType, eventMetadata);
    }

    public void sendMessage(MessageType messageType, EventMetadata<M> eventMetadata) {
        List<FPlayer> receivers = createReceivers(eventMetadata);
        sendMessage(messageType, receivers, eventMetadata);
    }

    public void sendMessage(List<FPlayer> receivers, EventMetadata<M> eventMetadata) {
        sendMessage(this.messageType, receivers, eventMetadata);
    }

    public void sendMessage(MessageType messageType, List<FPlayer> receivers, EventMetadata<M> eventMetadata) {
        if (receivers.isEmpty()) return;

        FEntity sender = eventMetadata.getSender();

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
                    sender,
                    receiver,
                    formatComponent,
                    subComponent,
                    eventMetadata
            ));
        });
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
        String formatContent = eventMetadata.resolveFormat(receiver, resolveLocalization(receiver));
        if (StringUtils.isEmpty(formatContent)) return Component.empty();

        FEntity sender = eventMetadata.getSender();
        boolean senderColorOut = eventMetadata.isSenderColorOut();

        MessagePipeline.Builder formatBuilder = messagePipeline
                .builder(eventMetadata.getMessageUUID(), sender, receiver, formatContent)
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
}
