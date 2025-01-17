package net.flectone.pulse.module;

import com.google.common.io.ByteArrayDataOutput;
import com.google.inject.Inject;
import lombok.Getter;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ProxyManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.*;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.MessageSender;
import net.flectone.pulse.platform.SoundPlayer;
import net.flectone.pulse.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

public abstract class AbstractModuleMessage<M extends Localization.ILocalization> extends AbstractModule {

    private final Function<Localization, M> messageResolver;

    @Inject private FPlayerManager fPlayerManager;
    @Inject private PermissionUtil permissionUtil;
    @Inject private FileManager fileManager;
    @Inject private ComponentUtil componentUtil;
    @Inject private TimeUtil timeUtil;
    @Inject private ModerationUtil moderationUtil;
    @Inject private ProxyManager proxyManager;
    @Inject private IntegrationModule integrationModule;
    @Inject private MessageSender messageSender;
    @Inject private SoundPlayer soundPlayer;
    @Inject private ThreadManager threadManager;

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
        if (!permissionUtil.has(fPlayer, sound.getPermission())) return;

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
        if (permissionUtil.has(fPlayer, getCooldown().getPermissionBypass())) return false;
        if (!getCooldown().isCooldown(fPlayer.getUuid())) return false;

        long timeLeft = getCooldown().getTimeLeft(fPlayer);

        builder(fPlayer)
                .format(s -> timeUtil.format(fPlayer, timeLeft, getCooldownMessage(fPlayer)))
                .sendBuilt();

        return true;
    }

    public boolean checkMute(@NotNull FEntity entity) {
        if (!(entity instanceof FPlayer fPlayer)) return false;
        if (!fPlayer.isMuted()) return false;

        builder(fPlayer)
                .format(s -> moderationUtil.buildMuteMessage(fPlayer))
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

    public Predicate<FPlayer> rangeFilter(FEntity sender, double range) {
        if (range == Range.PLAYER) {
            return sender::equals;
        }

        if (sender instanceof FPlayer fPlayer && !fPlayer.isUnknown()) {
            return fReceiver -> {
                if (fReceiver.isIgnored(fPlayer)) return false;

                if (range > 0) {
                    double distance = fPlayerManager.distance(fPlayer, fReceiver);

                    return distance != -1.0 && distance <= range;
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
        private UnaryOperator<ComponentUtil.Builder> formatComponentBuilder = null;
        private BiFunction<FPlayer, M, String> message = null;
        private UnaryOperator<ComponentUtil.Builder> messageComponentBuilder = null;
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

        public Builder formatBuilder(UnaryOperator<ComponentUtil.Builder> formatComponentBuilder) {
            this.formatComponentBuilder = formatComponentBuilder;
            return this;
        }

        public Builder messageBuilder(UnaryOperator<ComponentUtil.Builder> messageComponentBuilder) {
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

            return fPlayerManager.getFPlayersWithConsole().stream()
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
                    if (!permissionUtil.has(fPlayer, sound.getPermission())) return;

                    soundPlayer.play(sound, fReceiver);
                }
            });
        }

        private Component combine(Component format, Component message) {
            return format.replaceText(TextReplacementConfig.builder()
                    .match("<message>")
                    .replacement(builder -> message)
                    .build()
            );
        }

        private Component buildSubcomponent(FPlayer fReceiver) {
            return destination.getSubtext().isEmpty()
                    ? Component.empty()
                    : componentUtil.builder(fPlayer, fReceiver, destination.getSubtext()).build();
        }

        private Component buildFormatComponent(FPlayer fReceiver) {
            String format = resolveString(fReceiver, this.format);
            if (format == null) return Component.empty();

            ComponentUtil.Builder formatBuilder = componentUtil.builder(fPlayer, fReceiver, format)
                    .tagResolvers(tagResolvers == null ? null : tagResolvers.apply(fReceiver));

            if (formatComponentBuilder != null) {
                formatBuilder = formatComponentBuilder.apply(formatBuilder);
            }

            return formatBuilder.build();
        }

        private Component buildMessageComponent(FPlayer fReceiver) {
            String message = resolveString(fReceiver, this.message);
            if (message == null) return Component.empty();

            ComponentUtil.Builder messageBuilder = componentUtil.builder(fPlayer, fReceiver, message);

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

            Component component = componentUtil.builder(fPlayer, FPlayer.UNKNOWN, resolveString(FPlayer.UNKNOWN, format))
                    .tagResolvers(tagResolvers == null ? null : tagResolvers.apply(FPlayer.UNKNOWN))
                    .build();

            String message = resolveString(FPlayer.UNKNOWN, this.message);
            if (message != null) {
                component = component.replaceText(TextReplacementConfig.builder()
                        .match("<message>")
                        .replacement(componentUtil
                                .builder(fPlayer, FPlayer.UNKNOWN, message)
                                .userMessage(true)
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

            threadManager.runAsync(() -> integrationModule.sendMessage(fPlayer, tag, interfaceReplaceString));
        }

        public boolean sendToProxy() {
            if (tag == null) return false;
            if (proxyOutput == null) return false;
            if (range != Range.PROXY) return false;

            return proxyManager.sendMessage(fPlayer, tag, proxyOutput);
        }

        private String resolveString(FPlayer fPlayer, BiFunction<FPlayer, M, String> stringResolver) {
            if (stringResolver == null) return null;
            return stringResolver.apply(fPlayer, resolveLocalization(fPlayer));
        }
    }
}
