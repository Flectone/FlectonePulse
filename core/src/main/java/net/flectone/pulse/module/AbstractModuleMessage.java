package net.flectone.pulse.module;

import com.google.inject.Inject;
import lombok.Getter;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.checker.MuteChecker;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.formatter.ModerationMessageFormatter;
import net.flectone.pulse.formatter.TimeFormatter;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.*;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.sender.MessageSender;
import net.flectone.pulse.sender.ProxySender;
import net.flectone.pulse.sender.SoundPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.DataConsumer;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.Range;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public abstract class AbstractModuleMessage<M extends Localization.Localizable> extends AbstractModule {

    private final Function<Localization, M> messageResolver;

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
    @Inject private MessageSender messageSender;
    @Inject private SoundPlayer soundPlayer;
    @Inject private TaskScheduler taskScheduler;

    @Getter private Cooldown cooldown;
    @Getter private Sound sound;

    protected AbstractModuleMessage(Function<Localization, M> messageFunction) {
        this.messageResolver = messageFunction;
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
        return messageResolver.apply(fileResolver.getLocalization());
    }

    public M resolveLocalization(FEntity sender) {
        return messageResolver.apply(fileResolver.getLocalization(sender));
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

    public boolean sendDisableMessage(FEntity fPlayer, DisableAction whoDisableCommand) {
        return sendDisableMessage(fPlayer, fPlayer, whoDisableCommand);
    }

    public boolean sendDisableMessage(FEntity fPlayer, @NotNull FEntity fReceiver, DisableAction action) {
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

    public Predicate<FPlayer> rangeFilter(FEntity sender, int range) {
        if (range == Range.PLAYER) {
            return sender::equals;
        }

        if (!(sender instanceof FPlayer fPlayer) || fPlayer.isUnknown()) {
            return player -> true;
        }

        return createRangePredicate(fPlayer, range);
    }

    private Predicate<FPlayer> createRangePredicate(FPlayer fPlayer, int range) {
        return fReceiver -> {
            if (fReceiver.isUnknown()) return true;
            if (fReceiver.isIgnored(fPlayer)) return false;

            if (range > 0) {
                return checkDistance(fPlayer, fReceiver, range);
            }

            return switch (range) {
                case Range.WORLD_NAME -> checkWorldNamePermission(fPlayer, fReceiver);
                case Range.WORLD_TYPE -> checkWorldTypePermission(fPlayer, fReceiver);
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
        private MessageTag tag = null;
        private Integer range;
        private Destination destination = new Destination();
        private Sound sound = null;
        private Predicate<FPlayer> builderFilter = player -> true;
        private DataConsumer<DataOutputStream> proxyOutput = null;
        private UnaryOperator<String> integrationString = null;
        private BiFunction<FPlayer, M, String> format = null;
        private UnaryOperator<MessagePipeline.Builder> formatComponentBuilder = null;
        private BiFunction<FPlayer, M, String> message = null;
        private UnaryOperator<MessagePipeline.Builder> messageComponentBuilder = null;
        private Function<FPlayer, TagResolver[]> tagResolvers = null;

        public Builder(FEntity fEntity) {
            this.fPlayer = fEntity;
            range = Range.PLAYER;

            if (fEntity instanceof FPlayer builderFPlayer) {
                fReceiver = builderFPlayer;
            }
        }

        public Builder receiver(FPlayer fPlayer) {
            this.fReceiver = fPlayer;
            range = Range.PLAYER;
            return this;
        }

        public Builder tag(MessageTag tag) {
            this.tag = tag;
            return this;
        }

        public Builder range(int range) {
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

        public Builder proxy(DataConsumer<DataOutputStream> output) {
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
            recipients.forEach(recipient -> {

                // example
                // format: TheFaser > <message>
                // message: hello world!
                // final formatted message: TheFaser > hello world!
                Component messageComponent = buildMessageComponent(recipient);
                Component formatComponent = buildFormatComponent(recipient, messageComponent);

                // destination subtext
                Component subComponent = Component.empty();
                if (destination.getType() == Destination.Type.TITLE
                        || destination.getType() == Destination.Type.SUBTITLE) {
                    subComponent = buildSubcomponent(recipient, messageComponent);
                }

                messageSender.send(recipient, formatComponent, subComponent, destination);

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
                    .tagResolvers(messageTag(message))
                    .build();
        }

        private Component buildMessageComponent(FPlayer fReceiver) {
            String messageContent = resolveString(fReceiver, this.message);
            if (messageContent == null || messageContent.isBlank()) return Component.empty();

            MessagePipeline.Builder messageBuilder = messagePipeline.builder(fPlayer, fReceiver, messageContent)
                    .userMessage(true)
                    .mention(!fReceiver.isUnknown());

            if (messageComponentBuilder != null) {
                messageBuilder = messageComponentBuilder.apply(messageBuilder);
            }

            return messageBuilder.build();
        }

        private Component buildFormatComponent(FPlayer fReceiver, Component message) {
            String formatContent = resolveString(fReceiver, this.format);
            if (formatContent == null) return Component.empty();

            MessagePipeline.Builder formatBuilder = messagePipeline
                    .builder(fPlayer, fReceiver, formatContent)
                    .tagResolvers(tagResolvers == null ? null : tagResolvers.apply(fReceiver))
                    .tagResolvers(messageTag(message));

            if (!fReceiver.isUnknown()) {
                String messageToTranslate = resolveString(fReceiver, this.message);
                // support new <translate> and old <translateto>
                formatBuilder = formatBuilder.translate(messageToTranslate, formatContent.contains("<translate"));
            }

            if (formatComponentBuilder != null) {
                formatBuilder = formatComponentBuilder.apply(formatBuilder);
            }

            return formatBuilder.build();
        }

        public void sendToIntegrations() {
            if (tag == null) return;
            if (integrationString == null) return;
            if (range != Range.SERVER && range != Range.PROXY) return;
            if (!integrationModule.hasMessenger()) return;

            String formatContent = resolveString(FPlayer.UNKNOWN, format);
            if (formatContent == null) {
                formatContent = "";
            }

            Component componentFormat = messagePipeline.builder(fPlayer, FPlayer.UNKNOWN, formatContent)
                    .translate(false)
                    .tagResolvers(tagResolvers == null ? null : tagResolvers.apply(FPlayer.UNKNOWN))
                    .build();

            String messageContent = resolveString(FPlayer.UNKNOWN, this.message);
            Component componentMessage = messageContent == null
                    ? Component.empty()
                    : messagePipeline
                    .builder(fPlayer, FPlayer.UNKNOWN, messageContent)
                    .translate(false)
                    .userMessage(true)
                    .mention(false)
                    .interactiveChat(false)
                    .question(false)
                    .build();

            PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();
            String finalFormattedMessage = serializer.serialize(componentFormat)
                    .replace("<message>", serializer.serialize(componentMessage));

            UnaryOperator<String> interfaceReplaceString = s -> integrationString.apply(s)
                    .replace("<player>", fPlayer.getName())
                    .replace("<final_message>", finalFormattedMessage)
                    .replace("<final_clear_message>", finalClearMessagePattern.matcher(finalFormattedMessage).replaceAll(""));

            integrationModule.sendMessage(fPlayer, tag, interfaceReplaceString);
        }

        public boolean sendToProxy() {
            if (tag == null) return false;
            if (proxyOutput == null) return false;
            if (range != Range.PROXY) return false;

            return proxySender.send(fPlayer, tag, proxyOutput);
        }

        private String resolveString(FPlayer fPlayer, BiFunction<FPlayer, M, String> stringResolver) {
            if (stringResolver == null) return null;
            return stringResolver.apply(fPlayer, resolveLocalization(fPlayer));
        }
    }
}
