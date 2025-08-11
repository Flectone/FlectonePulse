package net.flectone.pulse.module.message.chat;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Cooldown;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.chat.listener.ChatPacketListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.PlatformType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Singleton
public class ChatModule extends AbstractModuleLocalization<Localization.Message.Chat> {

    private final Map<String, Cooldown> cooldownMap = new HashMap<>();
    private final Map<String, Sound> soundMap = new HashMap<>();

    private final Message.Chat message;
    private final Permission.Message.Chat permission;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PlatformServerAdapter platformServerAdapter;
    private final PermissionChecker permissionChecker;
    private final IntegrationModule integrationModule;
    private final TimeFormatter timeFormatter;
    private final Provider<BubbleModule> bubbleModuleProvider;
    private final Provider<SpyModule> spyModuleProvider;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public ChatModule(FileResolver fileResolver,
                      FPlayerService fPlayerService,
                      PlatformPlayerAdapter platformPlayerAdapter,
                      PlatformServerAdapter platformServerAdapter,
                      PermissionChecker permissionChecker,
                      IntegrationModule integrationModule,
                      TimeFormatter timeFormatter,
                      Provider<BubbleModule> bubbleModuleProvider,
                      Provider<SpyModule> spyModuleProvider,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getChat());

        this.message = fileResolver.getMessage().getChat();
        this.permission = fileResolver.getPermission().getMessage().getChat();
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.platformServerAdapter = platformServerAdapter;
        this.permissionChecker = permissionChecker;
        this.integrationModule = integrationModule;
        this.timeFormatter = timeFormatter;
        this.bubbleModuleProvider = bubbleModuleProvider;
        this.spyModuleProvider = spyModuleProvider;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        message.getTypes().forEach((key, value) -> {
            Permission.Message.Chat.Type permissions = permission.getTypes().get(key);
            if (permissions == null) return;

            registerPermission(permissions);
            cooldownMap.put(key, createCooldown(value.getCooldown(), permissions.getCooldownBypass()));
            soundMap.put(key, createSound(value.getSound(), permissions.getSound()));
        });

        if (message.getMode() == Message.Chat.Mode.PACKET || platformServerAdapter.getPlatformType() == PlatformType.FABRIC) {
            listenerRegistry.register(ChatPacketListener.class);
        }
    }

    @Override
    public void onDisable() {
        cooldownMap.clear();
        soundMap.clear();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public void send(FPlayer fPlayer, String eventMessage, Runnable cancelEvent, BiConsumer<String, Boolean> successEvent) {
        if (isModuleDisabledFor(fPlayer)) return;

        if (checkMute(fPlayer)) {
            cancelEvent.run();
            return;
        }

        Message.Chat.Type playerChat = getPlayerChat(fPlayer, eventMessage);

        var configChatEntry = message.getTypes().entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(playerChat))
                .findAny();

        if (playerChat == null || !playerChat.isEnable() || configChatEntry.isEmpty()) {
            builder(fPlayer)
                    .format(Localization.Message.Chat::getNullChat)
                    .sendBuilt();
            cancelEvent.run();
            return;
        }

        String chatName = configChatEntry.get().getKey();

        if (cooldownMap.containsKey(chatName)) {
            Cooldown cooldown = cooldownMap.get(chatName);
            if (cooldown != null
                    && cooldown.isEnable()
                    && !permissionChecker.check(fPlayer, cooldown.getPermissionBypass())
                    && cooldown.isCooldown(fPlayer.getUuid())) {
                long timeLeft = cooldownMap.get(chatName).getTimeLeft(fPlayer);

                builder(fPlayer)
                        .format(timeFormatter.format(fPlayer, timeLeft, getCooldownMessage(fPlayer)))
                        .sendBuilt();
                cancelEvent.run();
                return;
            }
        }

        String trigger = playerChat.getTrigger();

        if (trigger != null && !trigger.isEmpty() && eventMessage.startsWith(trigger)) {
            eventMessage = eventMessage.substring(trigger.length()).trim();
        }

        Predicate<FPlayer> chatPermissionFilter = fReceiver -> permissionChecker.check(fReceiver, permission.getTypes().get(chatName));

        Range chatRange = playerChat.getRange();

        // in local chat you can mention it too,
        // but I don't want to full support InteractiveChat
        String finalMessage = chatRange.is(Range.Type.PROXY)
                || chatRange.is(Range.Type.SERVER)
                || chatRange.is(Range.Type.WORLD_NAME)
                || chatRange.is(Range.Type.WORLD_TYPE)
                ? integrationModule.checkMention(fPlayer, eventMessage)
                : eventMessage;

        Builder builder = builder(fPlayer)
                .tag(MessageType.CHAT)
                .destination(playerChat.getDestination())
                .range(chatRange)
                .filter(chatPermissionFilter)
                .format(localization -> localization.getTypes().get(chatName))
                .message(finalMessage)
                .proxy(output -> {
                    output.writeUTF(chatName);
                    output.writeUTF(finalMessage);
                })
                .integration(s -> Strings.CS.replace(s, "<message>", finalMessage))
                .sound(soundMap.get(chatName));

        List<FPlayer> receivers = builder.build();

        builder.send(receivers);

        List<UUID> receiversUUID = receivers.stream()
                .filter(fReceiver -> !fReceiver.isUnknown())
                .filter(fReceiver -> integrationModule.isVanishedVisible(fReceiver, fPlayer))
                .map(FEntity::getUuid)
                .toList();

        spyModuleProvider.get().checkChat(fPlayer, chatName, finalMessage);

        int countReceivers = receiversUUID.size();
        Message.Chat.Type.NullReceiver nullReceiver = playerChat.getNullReceiver();

        checkReceiversLater(fPlayer, countReceivers, chatRange, receiversUUID, nullReceiver);

        successEvent.accept(finalMessage, playerChat.isCancel());

        bubbleModuleProvider.get().add(fPlayer, eventMessage);
    }


    @Async(delay = 5L)
    public void checkReceiversLater(FPlayer fPlayer, int countReceivers, Range chatRange,
                                    List<UUID> receiversUUID, Message.Chat.Type.NullReceiver nullReceiver) {
        if (!nullReceiver.isEnable() || countReceivers > 1) {
            return;
        }

        Set<UUID> onlinePlayers = fPlayerService.findOnlineFPlayers()
                .stream()
                .map(FEntity::getUuid)
                .collect(Collectors.toSet());

        if ((onlinePlayers.containsAll(receiversUUID) && onlinePlayers.size() <= countReceivers)
                || chatRange.is(Range.Type.BLOCKS)) {
            builder(fPlayer)
                    .destination(nullReceiver.getDestination())
                    .format(Localization.Message.Chat::getNullReceiver)
                    .sendBuilt();
        }
    }

    public void send(FEntity fPlayer, String chatName, String string) {
        if (isModuleDisabledFor(fPlayer)) return;

        var optionalChat = message.getTypes().entrySet().stream()
                .filter(chat -> chat.getKey().equals(chatName))
                .findAny();

        if (optionalChat.isEmpty()) return;

        String playerChatName = optionalChat.get().getKey();
        Message.Chat.Type playerChat = optionalChat.get().getValue();

        Predicate<FPlayer> filter = rangeFilter(fPlayer, playerChat.getRange()).and(fReceiver -> {
            if (!permissionChecker.check(fReceiver, permission.getTypes().get(playerChatName))) return false;

            return platformPlayerAdapter.isOnline(fReceiver);
        });

        builder(fPlayer)
                .range(Range.get(Range.Type.SERVER))
                .destination(playerChat.getDestination())
                .filter(filter)
                .format(s -> s.getTypes().get(playerChatName))
                .message(string)
                .sound(getSound())
                .sendBuilt();
    }

    private Message.Chat.Type getPlayerChat(FPlayer fPlayer, String eventMessage) {
        Message.Chat.Type playerChat = message.getTypes().get(fPlayer.getSettingValue(FPlayer.Setting.CHAT));

        // if that chat *does* have a trigger, return it
        if (playerChat != null && !StringUtils.isEmpty(playerChat.getTrigger())) {
            return playerChat;
        }

        int priority = Integer.MIN_VALUE;

        for (var entry : this.message.getTypes().entrySet()) {
            Message.Chat.Type chat = entry.getValue();
            String chatName = entry.getKey();

            if (!chat.isEnable()) continue;
            if (chat.getTrigger() != null
                    && !chat.getTrigger().isEmpty()
                    && !eventMessage.startsWith(chat.getTrigger())) continue;
            if (eventMessage.equals(chat.getTrigger())) continue;

            if (chat.getPriority() <= priority) continue;
            if (!permissionChecker.check(fPlayer, permission.getTypes().get(chatName))) continue;

            playerChat = chat;
            priority = chat.getPriority();
        }

        return playerChat;
    }
}
