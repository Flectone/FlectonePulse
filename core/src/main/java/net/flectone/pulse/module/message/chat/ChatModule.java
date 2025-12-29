package net.flectone.pulse.module.message.chat;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.chat.listener.ChatPacketListener;
import net.flectone.pulse.module.message.chat.model.Chat;
import net.flectone.pulse.module.message.chat.model.ChatMetadata;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.CooldownSender;
import net.flectone.pulse.platform.sender.DisableSender;
import net.flectone.pulse.platform.sender.MuteSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ChatModule extends AbstractModuleLocalization<Localization.Message.Chat> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlatformServerAdapter platformServerAdapter;
    private final PermissionChecker permissionChecker;
    private final IntegrationModule integrationModule;
    private final Provider<BubbleModule> bubbleModuleProvider;
    private final Provider<SpyModule> spyModuleProvider;
    private final ListenerRegistry listenerRegistry;
    private final TaskScheduler taskScheduler;
    private final MuteSender muteSender;
    private final DisableSender disableSender;
    private final CooldownSender cooldownSender;
    private final ProxyRegistry proxyRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        if (config().mode() == Message.Chat.Mode.PACKET || platformServerAdapter.getPlatformType() == PlatformType.FABRIC) {
            listenerRegistry.register(ChatPacketListener.class);
        }
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder()
                .addAll(permission().types().values())
                .addAll(permission().types().values().stream().map(Permission.Message.Chat.Type::sound).toList())
                .addAll(permission().types().values().stream().map(Permission.Message.Chat.Type::cooldownBypass).toList());
    }

    @Override
    public MessageType messageType() {
        return MessageType.CHAT;
    }

    @Override
    public Message.Chat config() {
        return fileFacade.message().chat();
    }

    @Override
    public Permission.Message.Chat permission() {
        return fileFacade.permission().message().chat();
    }

    @Override
    public Localization.Message.Chat localization(FEntity sender) {
        return fileFacade.localization(sender).message().chat();
    }

    public void handleChatEvent(FPlayer fPlayer, String eventMessage, Runnable cancelEvent, BiConsumer<String, Boolean> successEvent) {
        if (muteSender.sendIfMuted(fPlayer)) {
            cancelEvent.run();
            return;
        }

        if (disableSender.sendIfDisabled(fPlayer, fPlayer, messageType())) {
            cancelEvent.run();
            return;
        }

        Chat playerChat = getPlayerChat(fPlayer, eventMessage);
        if (playerChat.config() == null || !playerChat.config().enable()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Message.Chat::nullChat)
                    .build()
            );

            cancelEvent.run();
            return;
        }

        if (cooldownSender.sendIfCooldown(fPlayer, playerChat.cooldown())) {
            cancelEvent.run();
            return;
        }

        String trigger = playerChat.config().trigger();
        if (!StringUtils.isEmpty(trigger) && eventMessage.startsWith(trigger)) {
            eventMessage = eventMessage.substring(trigger.length()).trim();
        }

        Range chatRange = playerChat.config().range();

        // in local chat you can mention it too,
        // but I don't want to full support InteractiveChat
        String finalMessage = chatRange.is(Range.Type.PROXY)
                || chatRange.is(Range.Type.SERVER)
                || chatRange.is(Range.Type.WORLD_NAME)
                || chatRange.is(Range.Type.WORLD_TYPE)
                ? integrationModule.checkMention(fPlayer, eventMessage)
                : eventMessage;

        successEvent.accept(finalMessage, playerChat.config().cancel());

        sendMessage(fPlayer, eventMessage, finalMessage, playerChat);
    }

    public void sendMessage(FPlayer fPlayer, String eventMessage, String finalMessage, Chat playerChat) {
        String chatName = playerChat.name();

        ChatMetadata<Localization.Message.Chat> chatMetadata = ChatMetadata.<Localization.Message.Chat>builder()
                .sender(fPlayer)
                .format(localization -> localization.types().get(chatName))
                .chat(playerChat)
                .destination(playerChat.config().destination())
                .range(playerChat.config().range())
                .message(finalMessage)
                .sound(playerChat.sound())
                .filter(permissionFilter(chatName))
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeString(chatName);
                    dataOutputStream.writeString(finalMessage);
                })
                .integration()
                .build();

        List<FPlayer> receivers = createReceivers(messageType(), chatMetadata);

        sendMessage(receivers, chatMetadata);

        // send null receiver message
        if (playerChat.config().destination().type() != Destination.Type.CHAT) {
            checkReceiversLater(fPlayer, receivers, playerChat);
        } else {
            taskScheduler.runAsyncLater(() -> checkReceiversLater(fPlayer, receivers, playerChat), 1L);
        }

        // send to spy module
        spyModuleProvider.get().checkChat(fPlayer, chatName, finalMessage);

        // send to bubble module
        bubbleModuleProvider.get().add(fPlayer, eventMessage, receivers);
    }

    public Predicate<FPlayer> permissionFilter(String chatName) {
        return fReceiver -> permissionChecker.check(fReceiver, permission().types().get(chatName));
    }

    private void checkReceiversLater(FPlayer fPlayer, List<FPlayer> localReceivers, Chat playerChat) {
        if (!playerChat.config().nullReceiver().enable()) return;
        if (!noLocalReceiversFor(fPlayer, localReceivers)) return;

        if (playerChat.config().range().is(Range.Type.BLOCKS) || noGlobalReceiversFor(fPlayer, playerChat.name())) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Message.Chat::nullReceiver)
                    .destination(playerChat.config().nullReceiver().destination())
                    .build()
            );
        }
    }

    private boolean noLocalReceiversFor(FPlayer fPlayer, List<FPlayer> receivers) {
        return receivers.stream()
                .filter(fReceiver -> !fReceiver.isUnknown())
                .filter(fReceiver -> !fReceiver.equals(fPlayer))
                .noneMatch(fReceiver -> integrationModule.canSeeVanished(fReceiver, fPlayer));
    }

    private boolean noGlobalReceiversFor(FPlayer fPlayer, String chatName) {
        List<FPlayer> serverReceivers = fPlayerService.getOnlineFPlayers()
                .stream()
                .filter(filterReceivers(fPlayer, chatName))
                .toList();

        // check online server players first
        for (FPlayer fReceiver : serverReceivers) {
            if (!fReceiver.isIgnored(fPlayer) && fReceiver.isSetting(MessageType.CHAT)) {
                return false;
            }
        }

        if (proxyRegistry.hasEnabledProxy()) {
            List<FPlayer> proxyReceivers = fPlayerService.findOnlineFPlayers()
                    .stream()
                    .filter(fReceiver -> !serverReceivers.contains(fReceiver))
                    .filter(filterReceivers(fPlayer, chatName))
                    .toList();

            // check proxy players only if no online server receivers found
            for (FPlayer fReceiver : proxyReceivers) {
                fPlayerService.loadIgnores(fReceiver);
                fPlayerService.loadSettings(fReceiver);
                if (!fReceiver.isIgnored(fPlayer) && fReceiver.isSetting(MessageType.CHAT)) {
                    return false;
                }
            }
        }

        return true;
    }

    private Predicate<FPlayer> filterReceivers(FPlayer fPlayer, String chatName) {
        return fReceiver -> {
            if (fReceiver.isUnknown()) return false;
            if (fReceiver.equals(fPlayer)) return false;
            if (!integrationModule.canSeeVanished(fReceiver, fPlayer)) return false;

            return permissionFilter(chatName).test(fReceiver);
        };
    }

    private Chat getPlayerChat(FPlayer fPlayer, String eventMessage) {
        String returnedChatName = fPlayer.getSetting(SettingText.CHAT_NAME);
        Message.Chat.Type playerChat = config().types().get(returnedChatName);
        Permission.Message.Chat.Type chatPermission = permission().types().get(returnedChatName);

        // if that chat *does* have a trigger, return it
        if (playerChat != null && !StringUtils.isEmpty(playerChat.trigger())) {
            return new Chat(returnedChatName, playerChat, chatPermission);
        }

        int priority = Integer.MIN_VALUE;

        for (Map.Entry<String, Message.Chat.Type> entry : config().types().entrySet()) {
            Message.Chat.Type chat = entry.getValue();
            String chatName = entry.getKey();

            if (!chat.enable()) continue;
            if (chat.trigger() != null
                    && !chat.trigger().isEmpty()
                    && !eventMessage.startsWith(chat.trigger())) continue;
            if (eventMessage.equals(chat.trigger())) continue;

            if (chat.priority() <= priority) continue;
            if (!permissionChecker.check(fPlayer, permission().types().get(chatName))) continue;

            playerChat = chat;
            priority = chat.priority();
            returnedChatName = chatName;
        }

        return new Chat(returnedChatName, playerChat, permission().types().get(returnedChatName));
    }
}
