package net.flectone.pulse.module.message.chat;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
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
import net.flectone.pulse.module.message.chat.model.ChatMetadata;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.CooldownSender;
import net.flectone.pulse.platform.sender.DisableSender;
import net.flectone.pulse.platform.sender.MuteSender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.constant.SettingText;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.type.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@Singleton
public class ChatModule extends AbstractModuleLocalization<Localization.Message.Chat> {

    private final Map<String, Cooldown> cooldownMap = new HashMap<>();
    private final Map<String, Sound> soundMap = new HashMap<>();

    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PlatformServerAdapter platformServerAdapter;
    private final PermissionChecker permissionChecker;
    private final IntegrationModule integrationModule;
    private final Provider<BubbleModule> bubbleModuleProvider;
    private final Provider<SpyModule> spyModuleProvider;
    private final ListenerRegistry listenerRegistry;
    private final MuteSender muteSender;
    private final DisableSender disableSender;
    private final CooldownSender cooldownSender;

    @Inject
    public ChatModule(FileResolver fileResolver,
                      FPlayerService fPlayerService,
                      PlatformPlayerAdapter platformPlayerAdapter,
                      PlatformServerAdapter platformServerAdapter,
                      PermissionChecker permissionChecker,
                      IntegrationModule integrationModule,
                      Provider<BubbleModule> bubbleModuleProvider,
                      Provider<SpyModule> spyModuleProvider,
                      ListenerRegistry listenerRegistry,
                      MuteSender muteSender,
                      DisableSender disableSender,
                      CooldownSender cooldownSender) {
        super(MessageType.CHAT);

        this.fileResolver = fileResolver;
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.platformServerAdapter = platformServerAdapter;
        this.permissionChecker = permissionChecker;
        this.integrationModule = integrationModule;
        this.bubbleModuleProvider = bubbleModuleProvider;
        this.spyModuleProvider = spyModuleProvider;
        this.listenerRegistry = listenerRegistry;
        this.muteSender = muteSender;
        this.disableSender = disableSender;
        this.cooldownSender = cooldownSender;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        config().getTypes().forEach((key, value) -> {
            Permission.Message.Chat.Type permissions = permission().getTypes().get(key);
            if (permissions == null) return;

            registerPermission(permissions);
            cooldownMap.put(key, createCooldown(value.getCooldown(), permissions.getCooldownBypass()));
            soundMap.put(key, createSound(value.getSound(), permissions.getSound()));
        });

        if (config().getMode() == Message.Chat.Mode.PACKET || platformServerAdapter.getPlatformType() == PlatformType.FABRIC) {
            listenerRegistry.register(ChatPacketListener.class);
        }
    }

    @Override
    public void onDisable() {
        cooldownMap.clear();
        soundMap.clear();
    }

    @Override
    public Message.Chat config() {
        return fileResolver.getMessage().getChat();
    }

    @Override
    public Permission.Message.Chat permission() {
        return fileResolver.getPermission().getMessage().getChat();
    }

    @Override
    public Localization.Message.Chat localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getChat();
    }

    public void send(FPlayer fPlayer, String eventMessage, Runnable cancelEvent, BiConsumer<String, Boolean> successEvent) {
        if (muteSender.sendIfMuted(fPlayer)) {
            cancelEvent.run();
            return;
        }

        if (disableSender.sendIfDisabled(fPlayer, fPlayer, getMessageType())) {
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

        if (cooldownSender.sendIfCooldown(fPlayer, cooldownMap.get(chatName))) {
            cancelEvent.run();
            return;
        }

        String trigger = chatType.getTrigger();
        if (!StringUtils.isEmpty(trigger) && eventMessage.startsWith(trigger)) {
            eventMessage = eventMessage.substring(trigger.length()).trim();
        }

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
                .filter(permissionFilter(chatName))
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeString(chatName);
                    dataOutputStream.writeString(finalMessage);
                })
                .integration()
                .build();

        List<FPlayer> receivers = createReceivers(getMessageType(), chatMetadata);

        sendMessage(receivers, chatMetadata);

        successEvent.accept(finalMessage, playerChat.second().isCancel());

        // send null receiver message
        checkReceiversLater(fPlayer, receivers, playerChat);

        // send to spy module
        spyModuleProvider.get().checkChat(fPlayer, chatName, finalMessage);

        // send to bubble module
        bubbleModuleProvider.get().add(fPlayer, eventMessage);
    }

    @Async(delay = 1L)
    public void checkReceiversLater(FPlayer fPlayer, List<FPlayer> localReceivers, Pair<String, Message.Chat.Type> playerChat) {
        if (!playerChat.second().getNullReceiver().isEnable()) return;
        if (!noLocalReceiversFor(fPlayer, localReceivers)) return;

        if (playerChat.second().getRange().is(Range.Type.BLOCKS) || noGlobalReceiversFor(fPlayer, playerChat.first())) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Message.Chat::getNullReceiver)
                    .destination(playerChat.second().getNullReceiver().getDestination())
                    .build()
            );
        }
    }

    public Predicate<FPlayer> permissionFilter(String chatName) {
        return fReceiver -> permissionChecker.check(fReceiver, permission().getTypes().get(chatName));
    }

    private boolean noLocalReceiversFor(FPlayer fPlayer, List<FPlayer> receivers) {
        return receivers.stream()
                .filter(fReceiver -> !fReceiver.isUnknown())
                .filter(fReceiver -> !fReceiver.equals(fPlayer))
                .noneMatch(fReceiver -> integrationModule.canSeeVanished(fReceiver, fPlayer));
    }

    private boolean noGlobalReceiversFor(FPlayer fPlayer, String chatName) {
        List<FPlayer> fReceivers = fPlayerService.findOnlineFPlayers()
                .stream()
                .filter(fReceiver -> !fReceiver.isUnknown())
                .filter(fReceiver -> !fReceiver.equals(fPlayer))
                .filter(fReceiver -> integrationModule.canSeeVanished(fReceiver, fPlayer))
                .filter(permissionFilter(chatName))
                .toList();

        List<FPlayer> offlinePlayers = new ArrayList<>();

        // check online server players first
        for (FPlayer fReceiver : fReceivers) {
            if (!platformPlayerAdapter.isOnline(fReceiver)) {
                offlinePlayers.add(fReceiver);
                continue;
            }

            if (!fReceiver.isIgnored(fPlayer)) {
                return true;
            }

            if (fPlayerService.getFPlayer(fReceiver.getUuid()).isSetting(MessageType.CHAT)) {
                return true;
            }
        }

        // check proxy players only if no online server receivers found
        for (FPlayer fReceiver : offlinePlayers) {
            fPlayerService.loadIgnores(fReceiver);
            if (!fReceiver.isIgnored(fPlayer)) {
                return true;
            }

            fPlayerService.loadSettings(fReceiver);
            if (fReceiver.isSetting(MessageType.CHAT)) {
                return true;
            }
        }

        return false;
    }

    private Pair<String, Message.Chat.Type> getPlayerChat(FPlayer fPlayer, String eventMessage) {
        String returnedChatName = fPlayer.getSetting(SettingText.CHAT_NAME);
        Message.Chat.Type playerChat = config().getTypes().get(returnedChatName);

        // if that chat *does* have a trigger, return it
        if (playerChat != null && !StringUtils.isEmpty(playerChat.getTrigger())) {
            return Pair.of(returnedChatName, playerChat);
        }

        int priority = Integer.MIN_VALUE;

        for (Map.Entry<String, Message.Chat.Type> entry : config().getTypes().entrySet()) {
            Message.Chat.Type chat = entry.getValue();
            String chatName = entry.getKey();

            if (!chat.isEnable()) continue;
            if (chat.getTrigger() != null
                    && !chat.getTrigger().isEmpty()
                    && !eventMessage.startsWith(chat.getTrigger())) continue;
            if (eventMessage.equals(chat.getTrigger())) continue;

            if (chat.getPriority() <= priority) continue;
            if (!permissionChecker.check(fPlayer, permission().getTypes().get(chatName))) continue;

            playerChat = chat;
            priority = chat.getPriority();
            returnedChatName = chatName;
        }

        return Pair.of(returnedChatName, playerChat);
    }
}
