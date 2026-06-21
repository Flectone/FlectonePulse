package net.flectone.pulse.module.message.chat;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.IntegrationMetadata;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.chat.listener.ChatProxyMessageListener;
import net.flectone.pulse.module.message.chat.model.Chat;
import net.flectone.pulse.module.message.chat.model.ChatMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.CooldownSender;
import net.flectone.pulse.platform.sender.DisableSender;
import net.flectone.pulse.platform.sender.MuteSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ChatModule implements ModuleLocalization<Localization.Message.Chat> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final SocialService socialService;
    private final PermissionChecker permissionChecker;
    private final IntegrationModule integrationModule;
    private final Provider<BubbleModule> bubbleModuleProvider;
    private final Provider<SpyModule> spyModuleProvider;
    private final TaskScheduler taskScheduler;
    private final MuteSender muteSender;
    private final DisableSender disableSender;
    private final CooldownSender cooldownSender;
    private final MessageDispatcher messageDispatcher;
    private final ProxyRegistry proxyRegistry;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(ChatProxyMessageListener.class);
        }
    }

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleLocalization.super.permissionBuilder()
                .addAll(permission().types().values())
                .addAll(permission().types().values().stream().map(Permission.Message.Chat.Type::sound).toList())
                .addAll(permission().types().values().stream().map(Permission.Message.Chat.Type::cooldownBypass).toList());
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_CHAT;
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
    public Localization.Message.Chat localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().chat();
    }

    public void handleChatEvent(FPlayer fPlayer, String rawString, Runnable cancelEvent, BiConsumer<String, Boolean> successEvent) {
        if (muteSender.sendIfMuted(fPlayer)) {
            cancelEvent.run();
            return;
        }

        if (disableSender.sendIfDisabled(fPlayer, fPlayer, name())) {
            cancelEvent.run();
            return;
        }

        Chat playerChat = getPlayerChat(fPlayer, rawString);
        if (playerChat.config() == null || !playerChat.config().enable()) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Message.Chat>builder()
                    .sender(fPlayer)
                    .format(Localization.Message.Chat::nullChat)
                    .build()
            );

            cancelEvent.run();
            return;
        }

        if (cooldownSender.sendIfCooldown(fPlayer, playerChat.cooldown(), this.getClass().getName() + playerChat.name())) {
            cancelEvent.run();
            return;
        }

        String playerMessage = rawString;

        String trigger = playerChat.config().trigger();
        if (!StringUtils.isEmpty(trigger) && playerMessage.startsWith(trigger)) {
            playerMessage = playerMessage.substring(trigger.length()).trim();
        }

        Range chatRange = playerChat.config().range();

        // in local chat you can mention it too,
        // but I don't want to full support InteractiveChat
        playerMessage = chatRange.is(Range.Type.PROXY)
                || chatRange.is(Range.Type.SERVER)
                || chatRange.is(Range.Type.WORLD_NAME)
                || chatRange.is(Range.Type.WORLD_TYPE)
                ? integrationModule.checkMention(fPlayer, playerMessage)
                : playerMessage;

        successEvent.accept(playerMessage, playerChat.config().cancel());

        sendMessage(fPlayer, rawString, playerMessage, playerChat);
    }

    public void sendMessage(FPlayer fPlayer, String rawString, String playerMessage, Chat playerChat) {
        String chatName = playerChat.name();
        if (chatName == null) return;

        ChatMetadata<Localization.Message.Chat> chatMetadata = messageDispatcher.dispatch(this, ChatMetadata.<Localization.Message.Chat>builder()
                .base(EventMetadata.<Localization.Message.Chat>builder()
                        .sender(fPlayer)
                        .format(localization -> localization.types().get(chatName))
                        .destination(playerChat.config().destination())
                        .range(playerChat.config().range())
                        .message(playerMessage)
                        .sound(playerChat.sound())
                        .filter(permissionFilter(chatName))
                        .proxy(dataOutputStream -> {
                            dataOutputStream.writeString(chatName);
                            dataOutputStream.writeString(playerMessage);
                        })
                        .integration(IntegrationMetadata.builder()
                                .messageNames(List.of(name() + "_" + chatName.toUpperCase()))
                                .build()
                        )
                        .build()
                )
                .chat(playerChat)
                .build()
        );

        // send null receiver message
        if (playerChat.config().destination().type() != Destination.Type.CHAT) {
            checkReceiversLater(fPlayer, chatMetadata.receivers(), playerChat);
        } else {
            taskScheduler.runAsyncLater(() -> checkReceiversLater(fPlayer, chatMetadata.receivers(), playerChat), 1L);
        }

        // receivers can be empty due to proxy mode
        List<FPlayer> receiversWithSender = new ArrayList<>(chatMetadata.receivers());
        if (!receiversWithSender.contains(fPlayer)) {
            receiversWithSender.add(fPlayer);
        }

        // send to spy module
        spyModuleProvider.get().checkChat(fPlayer, chatName, playerMessage, receiversWithSender);

        // send to bubble module
        bubbleModuleProvider.get().add(fPlayer, rawString, playerMessage, receiversWithSender);
    }

    public Predicate<FPlayer> permissionFilter(String chatName) {
        return fReceiver -> permissionChecker.check(fReceiver, permission().types().get(chatName));
    }

    private void checkReceiversLater(FPlayer fPlayer, List<FPlayer> localReceivers, Chat playerChat) {
        if (!playerChat.config().nullReceiver().enable()) return;
        if (localReceivers.stream().anyMatch(filterReceivers(fPlayer, playerChat.name()))) return;

        if (playerChat.config().range().is(Range.Type.BLOCKS) || noGlobalReceiversFor(fPlayer, playerChat.name())) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Message.Chat>builder()
                    .sender(fPlayer)
                    .format(Localization.Message.Chat::nullReceiver)
                    .destination(playerChat.config().nullReceiver().destination())
                    .build()
            );
        }
    }

    private boolean noGlobalReceiversFor(FPlayer fPlayer, String chatName) {
        return fPlayerService.getOnlineFPlayers()
                .stream()
                .filter(filterReceivers(fPlayer, chatName))
                .noneMatch(fReceiver -> !socialService.isIgnored(fReceiver, fPlayer) && socialService.isSetting(fReceiver, ModuleName.MESSAGE_CHAT));
    }

    private Predicate<FPlayer> filterReceivers(FPlayer fPlayer, String chatName) {
        return fReceiver -> {
            if (fReceiver.isUnknown() || fReceiver.isConsole()) return false;
            if (fReceiver.equals(fPlayer)) return false;
            if (!socialService.canSeeVanished(fReceiver, fPlayer)) return false;

            return permissionFilter(chatName).test(fReceiver);
        };
    }

    private Chat getPlayerChat(FPlayer fPlayer, String eventMessage) {
        String returnedChatName = socialService.getSetting(fPlayer, SettingText.CHAT_NAME);
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
