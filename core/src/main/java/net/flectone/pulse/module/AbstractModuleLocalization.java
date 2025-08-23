package net.flectone.pulse.module;

import com.google.inject.Inject;
import lombok.Getter;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.metadata.MessageMetadata;
import net.flectone.pulse.model.event.message.SenderToReceiverMessageEvent;
import net.flectone.pulse.model.util.Cooldown;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.ProxyDataConsumer;
import net.flectone.pulse.util.checker.MuteChecker;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.DisableSource;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public abstract class AbstractModuleLocalization<M extends Localization.Localizable> extends AbstractModule {

    private final Function<Localization, M> localizationFunction;

    @Inject private FPlayerService fPlayerService;
    @Inject private ModerationService moderationService;
    @Inject private PlatformPlayerAdapter platformPlayerAdapter;
    @Inject private PermissionChecker permissionChecker;
    @Inject private MuteChecker muteChecker;
    @Inject private FileResolver fileResolver;
    @Inject private MessagePipeline messagePipeline;
    @Inject private ModerationMessageFormatter moderationMessageFormatter;
    @Inject private TimeFormatter timeFormatter;
    @Inject private ProxySender proxySender;
    @Inject private IntegrationModule integrationModule;
    @Inject private SoundPlayer soundPlayer;
    @Inject private TaskScheduler taskScheduler;
    @Inject private EventDispatcher eventDispatcher;

    @Getter private Cooldown cooldown;
    @Getter private Sound sound;

    protected AbstractModuleLocalization(Function<Localization, M> localizationMFunction) {
        this.localizationFunction = localizationMFunction;
    }

    public Sound createSound(Sound sound, Permission.IPermission permission) {
        this.sound = sound;

        if (permission != null) {
            registerPermission(permission);
            sound.setPermission(permission.getName());
        }

        return sound;
    }

    public void playSound(FPlayer fPlayer) {
        if (!permissionChecker.check(fPlayer, sound.getPermission())) return;

        soundPlayer.play(sound, fPlayer);
    }

    public Cooldown createCooldown(Cooldown cooldown, Permission.IPermission permission) {
        this.cooldown = cooldown;

        if (permission != null) {
            registerPermission(permission);
            cooldown.setPermissionBypass(permission.getName());
        }

        return this.cooldown;
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

    public Builder builder(FEntity fPlayer) {
        return new Builder(fPlayer);
    }

    public boolean checkCooldown(FEntity entity) {
        if (getCooldown() == null) return false;
        if (!getCooldown().isEnable()) return false;
        if (!(entity instanceof FPlayer fPlayer)) return false;
        if (permissionChecker.check(fPlayer, getCooldown().getPermissionBypass())) return false;
        if (!getCooldown().isCooldown(fPlayer.getUuid())) return false;

        long timeLeft = getCooldown().getTimeLeft(fPlayer);

        builder(fPlayer)
                .format(s -> timeFormatter.format(fPlayer, timeLeft, getCooldownMessage(fPlayer)))
                .sendBuilt();

        return true;
    }

    public boolean checkMute(@NotNull FEntity entity) {
        if (!(entity instanceof FPlayer fPlayer)) return false;

        MuteChecker.Status status = muteChecker.check(fPlayer);
        if (status == MuteChecker.Status.NONE) return false;

        builder(fPlayer)
                .format(s -> moderationMessageFormatter.buildMuteMessage(fPlayer, status))
                .sendBuilt();

        return true;
    }

    public boolean sendDisableMessage(FEntity fPlayer, DisableSource whoDisableCommand) {
        return sendDisableMessage(fPlayer, fPlayer, whoDisableCommand);
    }

    public boolean sendDisableMessage(FEntity fPlayer, @NotNull FEntity fReceiver, DisableSource action) {
        Localization.Command.Chatsetting.Disable localization = fileResolver.getLocalization(fReceiver).getCommand().getChatsetting().getDisable();

        String string = switch (action) {
            case HE -> localization.getHe();
            case YOU -> localization.getYou();
            case SERVER -> localization.getServer();
        };

        builder(fPlayer)
                .format(string)
                .sendBuilt();

        return true;
    }

    public boolean checkIgnore(FPlayer fSender, FPlayer fReceiver) {
        Localization.Command.Ignore localization = fileResolver.getLocalization(fSender).getCommand().getIgnore();

        if (fSender.isIgnored(fReceiver)) {
            builder(fSender)
                    .format(localization.getYou())
                    .sendBuilt();

            return true;
        }

        if (fReceiver.isIgnored(fSender)) {
            builder(fSender)
                    .format(localization.getHe())
                    .sendBuilt();

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

    public class Builder {

        private static final Pattern finalClearMessagePattern = Pattern.compile("[\\p{C}\\p{So}\\x{E0100}-\\x{E01EF}]+");

        private final FEntity fPlayer;
        private FPlayer fReceiver = FPlayer.UNKNOWN;
        private MessageType tag = null;
        private Range range;
        private Destination destination = new Destination();
        private Sound sound = null;
        private Predicate<FPlayer> builderFilter = player -> true;
        private ProxyDataConsumer<DataOutputStream> proxyOutput = null;
        private UnaryOperator<String> integrationString = null;
        private BiFunction<FPlayer, M, String> format = null;
        private UnaryOperator<MessagePipeline.Builder> formatComponentBuilder = null;
        private BiFunction<FPlayer, M, String> message = null;
        private UnaryOperator<MessagePipeline.Builder> messageComponentBuilder = null;
        private MessageMetadata metadata;
        private Function<FPlayer, TagResolver[]> tagResolvers = null;
        private boolean senderColorOut = true;

        public Builder(FEntity fEntity) {
            this.fPlayer = fEntity;
            this.range = Range.get(Range.Type.PLAYER);

            if (fEntity instanceof FPlayer builderFPlayer) {
                fReceiver = builderFPlayer;
            }
        }

        public Builder receiver(FPlayer fPlayer) {
            return receiver(fPlayer, false);
        }

        public Builder receiver(FPlayer fPlayer, boolean senderColorOut) {
            this.fReceiver = fPlayer;
            this.range = Range.get(Range.Type.PLAYER);
            this.senderColorOut = senderColorOut;
            return this;
        }

        public Builder tag(MessageType tag) {
            this.tag = tag;
            return this;
        }

        public Builder range(Range range) {
            this.range = range;
            return this;
        }

        public Builder destination(Destination destination) {
            this.destination = destination;
            return this;
        }

        public Builder filter(Predicate<FPlayer> filter) {
            this.builderFilter = this.builderFilter.and(filter);
            return this;
        }

        public Builder proxy() {
            this.proxyOutput = dataOutputStream -> {};
            return this;
        }

        public Builder proxy(ProxyDataConsumer<DataOutputStream> output) {
            this.proxyOutput = output;
            return this;
        }

        public Builder format(String format) {
            this.format = (fResolver, s) -> format;
            return this;
        }

        public Builder format(Function<M, String> format) {
            this.format = (fResolver, s) -> format.apply(s);
            return this;
        }

        public Builder format(BiFunction<FPlayer, M, String> format) {
            this.format = format;
            return this;
        }

        public Builder message(String message) {
            this.message = (fResolver, s) -> message;
            return this;
        }

        public Builder message(Function<M, String> message) {
            this.message = (fResolver, s) -> message.apply(s);
            return this;
        }

        public Builder message(BiFunction<FPlayer, M, String> message) {
            this.message = message;
            return this;
        }

        public Builder integration() {
            this.integrationString = s -> s;
            return this;
        }

        public Builder integration(UnaryOperator<String> integrationString) {
            this.integrationString = integrationString;
            return this;
        }

        public Builder tagResolvers(Function<FPlayer, TagResolver[]> tagResolvers) {
            this.tagResolvers = tagResolvers;
            return this;
        }

        public Builder addMetadata(MessageMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder formatBuilder(UnaryOperator<MessagePipeline.Builder> formatComponentBuilder) {
            this.formatComponentBuilder = formatComponentBuilder;
            return this;
        }

        public Builder messageBuilder(UnaryOperator<MessagePipeline.Builder> messageComponentBuilder) {
            this.messageComponentBuilder = messageComponentBuilder;
            return this;
        }

        public Builder sound(Sound sound) {
            this.sound = sound;
            return this;
        }

        public void sendBuilt() {
            send(build());
        }

        public List<FPlayer> build() {
            taskScheduler.runAsync(this::sendToIntegrations);

            // proxy sent message for all servers
            if (sendToProxy()) {
                return new ArrayList<>();
            }

            return fPlayerService.getFPlayersWithConsole().stream()
                    .filter(builderFilter)
                    .filter(rangeFilter(fReceiver, range))
                    .toList();
        }

        public void send(List<FPlayer> recipients) {
            UUID messageUUID = UUID.randomUUID();

            recipients.forEach(recipient -> {

                // example
                // format: TheFaser > <message>
                // message: hello world!
                // final formatted message: TheFaser > hello world!
                Component messageComponent = buildMessageComponent(recipient);
                Component formatComponent = buildFormatComponent(messageUUID, recipient, messageComponent);

                // destination subtext
                Component subComponent = Component.empty();
                if (destination.getType() == Destination.Type.TITLE
                        || destination.getType() == Destination.Type.SUBTITLE) {
                    subComponent = buildSubcomponent(recipient, messageComponent);
                }

                eventDispatcher.dispatch(new SenderToReceiverMessageEvent(messageUUID,
                        fPlayer,
                        recipient,
                        formatComponent,
                        subComponent,
                        destination,
                        metadata
                ));

                if (sound != null) {
                    if (!permissionChecker.check(fPlayer, sound.getPermission())) return;

                    soundPlayer.play(sound, recipient);
                }
            });
        }

        private TagResolver messageTag(Component message) {
            return TagResolver.resolver("message", (argumentQueue, context) -> Tag.inserting(message));
        }

        private Component buildSubcomponent(FPlayer fReceiver, Component message) {
            return destination.getSubtext().isEmpty()
                    ? Component.empty()
                    : messagePipeline.builder(fPlayer, fReceiver, destination.getSubtext())
                    .flag(MessageFlag.SENDER_COLOR_OUT, senderColorOut)
                    .tagResolvers(messageTag(message))
                    .build();
        }

        private Component buildMessageComponent(FPlayer fReceiver) {
            String messageContent = resolveString(fReceiver, this.message);
            if (StringUtils.isEmpty(messageContent)) return Component.empty();

            MessagePipeline.Builder messageBuilder = messagePipeline.builder(fPlayer, fReceiver, messageContent)
                    .flag(MessageFlag.USER_MESSAGE, true)
                    .flag(MessageFlag.SENDER_COLOR_OUT, senderColorOut)
                    .flag(MessageFlag.MENTION, !fReceiver.isUnknown());

            if (messageComponentBuilder != null) {
                messageBuilder = messageComponentBuilder.apply(messageBuilder);
            }

            return messageBuilder.build();
        }

        private Component buildFormatComponent(UUID messageUUID, FPlayer fReceiver, Component message) {
            String formatContent = resolveString(fReceiver, this.format);
            if (StringUtils.isEmpty(formatContent)) return Component.empty();

            MessagePipeline.Builder formatBuilder = messagePipeline
                    .builder(messageUUID, fPlayer, fReceiver, formatContent)
                    .flag(MessageFlag.SENDER_COLOR_OUT, senderColorOut)
                    .tagResolvers(tagResolvers == null ? null : tagResolvers.apply(fReceiver))
                    .tagResolvers(messageTag(message));

            if (!fReceiver.isUnknown()) {
                formatBuilder = formatBuilder
                        .setUserMessage(resolveString(fReceiver, this.message))
                        .translate(formatContent.contains("<translate")); // support new <translate> and old <translateto>
            }

            if (formatComponentBuilder != null) {
                formatBuilder = formatComponentBuilder.apply(formatBuilder);
            }

            return formatBuilder.build();
        }

        public void sendToIntegrations() {
            if (tag == null) return;
            if (integrationString == null) return;
            if (!range.is(Range.Type.SERVER) && !range.is(Range.Type.PROXY)) return;
            if (!integrationModule.hasMessenger()) return;

            String formatContent = resolveString(FPlayer.UNKNOWN, format);
            if (formatContent == null) {
                formatContent = "";
            }

            Component componentFormat = messagePipeline.builder(fPlayer, FPlayer.UNKNOWN, formatContent)
                    .flag(MessageFlag.SENDER_COLOR_OUT, senderColorOut)
                    .flag(MessageFlag.TRANSLATE, false)
                    .tagResolvers(tagResolvers == null ? null : tagResolvers.apply(FPlayer.UNKNOWN))
                    .build();

            String messageContent = resolveString(FPlayer.UNKNOWN, this.message);
            Component componentMessage = StringUtils.isEmpty(messageContent)
                    ? Component.empty()
                    : messagePipeline
                    .builder(fPlayer, FPlayer.UNKNOWN, messageContent)
                    .flag(MessageFlag.SENDER_COLOR_OUT, senderColorOut)
                    .flag(MessageFlag.TRANSLATE, false)
                    .flag(MessageFlag.USER_MESSAGE, true)
                    .flag(MessageFlag.MENTION, false)
                    .flag(MessageFlag.INTERACTIVE_CHAT, false)
                    .flag(MessageFlag.QUESTION, false)
                    .build();

            PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();
            String finalFormattedMessage = Strings.CS.replace(
                    serializer.serialize(componentFormat),
                    "<message>",
                    serializer.serialize(componentMessage)
            );

            UnaryOperator<String> interfaceReplaceString = s -> {
                String input = integrationString.apply(s);
                if (StringUtils.isBlank(input)) return StringUtils.EMPTY;

                String clearMessage = RegExUtils.replaceAll((CharSequence) finalFormattedMessage, finalClearMessagePattern, StringUtils.EMPTY);
                return StringUtils.replaceEach(
                        input,
                        new String[]{"<player>", "<final_message>", "<final_clear_message>"},
                        new String[]{fPlayer.getName(), finalFormattedMessage, clearMessage}
                );
            };

            integrationModule.sendMessage(fPlayer, tag, interfaceReplaceString);
        }

        public boolean sendToProxy() {
            if (tag == null) return false;
            if (proxyOutput == null) return false;
            if (!range.is(Range.Type.PROXY)) return false;

            return proxySender.send(fPlayer, tag, proxyOutput);
        }

        private String resolveString(FPlayer fPlayer, BiFunction<FPlayer, M, String> stringResolver) {
            if (stringResolver == null) return null;
            return stringResolver.apply(fPlayer, resolveLocalization(fPlayer));
        }
    }
}
