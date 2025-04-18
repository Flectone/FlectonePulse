package net.flectone.pulse.module;

import com.google.common.io.ByteArrayDataOutput;
import com.google.inject.Inject;
import lombok.Getter;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.checker.MuteChecker;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.formatter.ModerationMessageFormatter;
import net.flectone.pulse.formatter.TimeFormatter;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.*;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.sender.MessageSender;
import net.flectone.pulse.sender.ProxySender;
import net.flectone.pulse.sender.SoundPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.Range;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

public abstract class AbstractModuleMessage<M extends Localization.Localizable> extends AbstractModule {

    private final Function<Localization, M> messageResolver;

    @Inject private FPlayerService fPlayerService;
    @Inject private ModerationService moderationService;
    @Inject private PlatformPlayerAdapter platformPlayerAdapter;
    @Inject private PermissionChecker permissionChecker;
    @Inject private MuteChecker muteChecker;
    @Inject private FileManager fileManager;
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

    public AbstractModuleMessage(Function<Localization, M> messageFunction) {
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
        return fileManager.getLocalization(sender).getCooldown();
    }

    public M resolveLocalization() {
        return messageResolver.apply(fileManager.getLocalization());
    }

    public M resolveLocalization(FEntity sender) {
        return messageResolver.apply(fileManager.getLocalization(sender));
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
        Localization.Command.Chatsetting.Disable localization = fileManager.getLocalization(fReceiver).getCommand().getChatsetting().getDisable();

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
        Localization.Command.Ignore localization = fileManager.getLocalization(fSender).getCommand().getIgnore();

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

        if (sender instanceof FPlayer fPlayer && !fPlayer.isUnknown()) {
            return fReceiver -> {
                if (fReceiver.isUnknown()) return true;
                if (fReceiver.isIgnored(fPlayer)) return false;

                if (range > 0) {
                    double distance = platformPlayerAdapter.distance(fPlayer, fReceiver);

                    return distance != -1.0 && distance <= range;
                }

                if (range == Range.WORLD_NAME) {
                    String worldName = platformPlayerAdapter.getWorldName(fPlayer);
                    if (worldName.isEmpty()) return true;

                    return permissionChecker.check(fReceiver, "flectonepulse.world.name." + worldName);
                }

                if (range == Range.WORLD_TYPE) {
                    String worldType = platformPlayerAdapter.getWorldEnvironment(fPlayer);
                    if (worldType.isEmpty()) return true;

                    return permissionChecker.check(fReceiver, "flectonepulse.world.type." + worldType);
                }

                return true;
            };
        }

        return fPlayer -> true;
    }

    public class Builder {

        private final FEntity fPlayer;
        private FPlayer fReceiver = FPlayer.UNKNOWN;
        private MessageTag tag = null;
        private Integer range;
        private Destination destination = new Destination();
        private Sound sound = null;
        private Predicate<FPlayer> builderFilter = player -> true;
        private Consumer<ByteArrayDataOutput> proxyOutput = null;
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
            this.proxyOutput = byteArrayDataOutput -> {};
            return this;
        }

        public Builder proxy(Consumer<ByteArrayDataOutput> output) {
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
            sendToIntegrations();

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
            recipients.forEach(fReceiver -> {

                // user message
                // example for chat message
                Component message = buildMessageComponent(fReceiver);

                // example
                // format: TheFaser > <message>
                // message: hello world!
                // final formatted message: TheFaser > hello world!
                Component component = combine(buildFormatComponent(fReceiver), message);

                // destination subtext
                Component subcomponent = Component.empty();
                if (destination.getType() == Destination.Type.TITLE
                        || destination.getType() == Destination.Type.SUBTITLE) {
                    subcomponent = combine(buildSubcomponent(fReceiver), message);
                }

                messageSender.send(fReceiver, component, subcomponent, destination);

                if (sound != null) {
                    if (!permissionChecker.check(fPlayer, sound.getPermission())) return;

                    soundPlayer.play(sound, fReceiver);
                }
            });
        }

        private Component combine(Component format, Component message) {
            return format.replaceText(TextReplacementConfig.builder()
                    .match("<message>")
                    .replacement(message)
                    .build()
            );
        }

        private Component buildSubcomponent(FPlayer fReceiver) {
            return destination.getSubtext().isEmpty()
                    ? Component.empty()
                    : messagePipeline.builder(fPlayer, fReceiver, destination.getSubtext()).build();
        }

        private Component buildFormatComponent(FPlayer fReceiver) {
            String format = resolveString(fReceiver, this.format);
            if (format == null) return Component.empty();

            MessagePipeline.Builder formatBuilder = messagePipeline
                    .builder(fPlayer, fReceiver, format)
                    .translate(resolveString(fReceiver, this.message), format.contains("message_to_translate"))
                    .tagResolvers(tagResolvers == null ? null : tagResolvers.apply(fReceiver));

            if (formatComponentBuilder != null) {
                formatBuilder = formatComponentBuilder.apply(formatBuilder);
            }

            return formatBuilder.build();
        }

        private Component buildMessageComponent(FPlayer fReceiver) {
            String message = resolveString(fReceiver, this.message);
            if (message == null) return Component.empty();

            MessagePipeline.Builder messageBuilder = messagePipeline.builder(fPlayer, fReceiver, message);

            if (messageComponentBuilder != null) {
                messageBuilder = messageComponentBuilder.apply(messageBuilder);
            } else {
                messageBuilder = messageBuilder
                        .userMessage(true)
                        .mention(true);
            }

            return messageBuilder.build();
        }

        public void sendToIntegrations() {
            if (tag == null) return;
            if (integrationString == null) return;
            if (range != Range.SERVER && range != Range.PROXY) return;
            if (!integrationModule.hasMessenger()) return;

            Component component = messagePipeline.builder(fPlayer, FPlayer.UNKNOWN, resolveString(FPlayer.UNKNOWN, format))
                    .tagResolvers(tagResolvers == null ? null : tagResolvers.apply(FPlayer.UNKNOWN))
                    .build();

            String message = resolveString(FPlayer.UNKNOWN, this.message);
            if (message != null) {
                component = component.replaceText(TextReplacementConfig.builder()
                        .match("<message>")
                        .replacement(messagePipeline
                                .builder(fPlayer, FPlayer.UNKNOWN, message)
                                .userMessage(true)
                                .mention(false)
                                .interactiveChat(false)
                                .question(false)
                                .build()
                        )
                        .build()
                );
            }

            String finalMessage = PlainTextComponentSerializer.plainText().serialize(component);
            UnaryOperator<String> interfaceReplaceString = s -> integrationString.apply(s)
                    .replace("<player>", fPlayer.getName())
                    .replace("<final_message>", finalMessage)
                    .replace("<final_clear_message>", finalMessage.replaceAll("[\\p{C}\\p{So}\\x{E0100}-\\x{E01EF}]+", ""));

            taskScheduler.runAsync(() -> integrationModule.sendMessage(fPlayer, tag, interfaceReplaceString));
        }

        public boolean sendToProxy() {
            if (tag == null) return false;
            if (proxyOutput == null) return false;
            if (range != Range.PROXY) return false;

            return proxySender.sendMessage(fPlayer, tag, proxyOutput);
        }

        private String resolveString(FPlayer fPlayer, BiFunction<FPlayer, M, String> stringResolver) {
            if (stringResolver == null) return null;
            return stringResolver.apply(fPlayer, resolveLocalization(fPlayer));
        }
    }
}
