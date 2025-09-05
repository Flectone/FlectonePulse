package net.flectone.pulse.module.message.chat;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Cooldown;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.chat.listener.ChatPacketListener;
import net.flectone.pulse.module.message.chat.model.ChatMetadata;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.constant.SettingText;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.type.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

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
                      Provider<BubbleModule> bubbleModuleProvider,
                      Provider<SpyModule> spyModuleProvider,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getChat(), MessageType.CHAT);

        this.message = fileResolver.getMessage().getChat();
        this.permission = fileResolver.getPermission().getMessage().getChat();
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.platformServerAdapter = platformServerAdapter;
        this.permissionChecker = permissionChecker;
        this.integrationModule = integrationModule;
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
        if (checkMute(fPlayer)) {
            cancelEvent.run();
            return;
        }

        Pair<String, Message.Chat.Type> playerChat = getPlayerChat(fPlayer, eventMessage);

        String chatName = playerChat.first();
        Message.Chat.Type chatType = playerChat.second();

        if (chatType == null || !chatType.isEnable()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Message.Chat::getNullChat)
                    .build()
            );

            cancelEvent.run();
            return;
        }

        if (checkCooldown(fPlayer, cooldownMap.get(chatName))) {
            cancelEvent.run();
            return;
        }

        String trigger = chatType.getTrigger();
        if (!StringUtils.isEmpty(trigger) && eventMessage.startsWith(trigger)) {
            eventMessage = eventMessage.substring(trigger.length()).trim();
        }

        Predicate<FPlayer> chatPermissionFilter = fReceiver -> permissionChecker.check(fReceiver, permission.getTypes().get(chatName));

        Range chatRange = chatType.getRange();

        // in local chat you can mention it too,
        // but I don't want to full support InteractiveChat
        String finalMessage = chatRange.is(Range.Type.PROXY)
                || chatRange.is(Range.Type.SERVER)
                || chatRange.is(Range.Type.WORLD_NAME)
                || chatRange.is(Range.Type.WORLD_TYPE)
                ? integrationModule.checkMention(fPlayer, eventMessage)
                : eventMessage;


        ChatMetadata<Localization.Message.Chat> chatMetadata = ChatMetadata.<Localization.Message.Chat>builder()
                .sender(fPlayer)
                .format(localization -> localization.getTypes().get(chatName))
                .chatName(chatName)
                .chatType(playerChat.second())
                .destination(playerChat.second().getDestination())
                .range(chatRange)
                .message(finalMessage)
                .sound(soundMap.get(chatName))
                .filter(chatPermissionFilter)
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeString(chatName);
                    dataOutputStream.writeString(finalMessage);
                })
                .integration()
                .build();

        List<FPlayer> receivers = createReceivers(MessageType.CHAT, chatMetadata);

        sendMessage(receivers, chatMetadata);

        int receiversCount = (int) receivers.stream()
                .filter(fReceiver -> !fReceiver.isUnknown())
                .filter(fReceiver -> !fReceiver.equals(fPlayer))
                .filter(fReceiver -> integrationModule.canSeeVanished(fReceiver, fPlayer))
                .count();

        spyModuleProvider.get().checkChat(fPlayer, chatName, finalMessage);

        checkReceiversLater(fPlayer, chatRange, receiversCount, playerChat.second().getNullReceiver());

        successEvent.accept(finalMessage, playerChat.second().isCancel());

        bubbleModuleProvider.get().add(fPlayer, eventMessage);
    }

    @Async(delay = 1L)
    public void checkReceiversLater(FPlayer fPlayer,
                                    Range chatRange,
                                    int receiversCount,
                                    Message.Chat.Type.NullReceiver nullReceiver) {
        if (!nullReceiver.isEnable() || receiversCount != 0) {
            return;
        }

        int onlinePlayersCount = (int) fPlayerService.findOnlineFPlayers()
                .stream()
                .filter(fReceiver -> !fReceiver.isUnknown())
                .filter(fReceiver -> !fReceiver.equals(fPlayer))
                .filter(fReceiver -> integrationModule.canSeeVanished(fReceiver, fPlayer))
                .count();

        if (chatRange.is(Range.Type.BLOCKS) || onlinePlayersCount == 0) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Message.Chat::getNullReceiver)
                    .destination(nullReceiver.getDestination())
                    .build()
            );
        }
    }

    public void send(FPlayer fPlayer, String proxyChatName, String string, UUID metadataUUID) {
        if (isModuleDisabledFor(fPlayer)) return;

        var optionalChat = message.getTypes().entrySet().stream()
                .filter(chat -> chat.getKey().equals(proxyChatName))
                .findAny();

        if (optionalChat.isEmpty()) return;

        String chatName = optionalChat.get().getKey();
        Message.Chat.Type chatType = optionalChat.get().getValue();

        Predicate<FPlayer> filter = rangeFilter(fPlayer, chatType.getRange()).and(fReceiver -> {
            if (!permissionChecker.check(fReceiver, permission.getTypes().get(chatName))) return false;

            return platformPlayerAdapter.isOnline(fReceiver);
        });

        sendMessage(ChatMetadata.<Localization.Message.Chat>builder()
                .uuid(metadataUUID)
                .sender(fPlayer)
                .format(s -> s.getTypes().get(chatName))
                .chatName(chatName)
                .chatType(chatType)
                .range(Range.get(Range.Type.SERVER))
                .destination(chatType.getDestination())
                .message(string)
                .sound(getModuleSound())
                .filter(filter)
                .build()
        );
    }

    private Pair<String, Message.Chat.Type> getPlayerChat(FPlayer fPlayer, String eventMessage) {
        String returnedChatName = fPlayer.getSetting(SettingText.CHAT_NAME);
        Message.Chat.Type playerChat = message.getTypes().get(returnedChatName);

        // if that chat *does* have a trigger, return it
        if (playerChat != null && !StringUtils.isEmpty(playerChat.getTrigger())) {
            return Pair.of(returnedChatName, playerChat);
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
            returnedChatName = chatName;
        }

        return Pair.of(returnedChatName, playerChat);
    }
}
