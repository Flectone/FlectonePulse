package net.flectone.pulse.module.message.chat;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.formatter.TimeFormatter;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.Cooldown;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.bubble.BukkitBubbleModule;
import net.flectone.pulse.module.message.chat.listener.ChatListener;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.Range;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Singleton
public class BukkitChatModule extends ChatModule {

    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final BukkitListenerRegistry bukkitListenerManager;
    private final IntegrationModule integrationModule;
    private final TimeFormatter timeFormatter;
    private final Provider<BukkitBubbleModule> bubbleModuleProvider;
    private final Provider<SpyModule> spyModuleProvider;

    @Inject
    public BukkitChatModule(FileManager fileManager,
                            FPlayerService fPlayerService,
                            BukkitListenerRegistry bukkitListenerManager,
                            IntegrationModule integrationModule,
                            PermissionChecker permissionChecker,
                            TimeFormatter timeFormatter,
                            Provider<BukkitBubbleModule> bubbleModuleProvider,
                            Provider<SpyModule> spyModuleProvider) {
        super(fileManager);

        this.fPlayerService = fPlayerService;
        this.bukkitListenerManager = bukkitListenerManager;
        this.integrationModule = integrationModule;
        this.permissionChecker = permissionChecker;
        this.timeFormatter = timeFormatter;
        this.bubbleModuleProvider = bubbleModuleProvider;
        this.spyModuleProvider = spyModuleProvider;
    }

    @Override
    public void reload() {
        super.reload();

        bukkitListenerManager.register(ChatListener.class, EventPriority.NORMAL);
    }

    @Override
    public void send(FPlayer fPlayer, Object chatEvent) {
        if (!(chatEvent instanceof AsyncPlayerChatEvent event)) return;
        if (checkModulePredicates(fPlayer)) return;

        if (checkMute(fPlayer)) {
            event.getRecipients().clear();
            event.setCancelled(true);
            return;
        }

        String eventMessage = event.getMessage();

        Message.Chat.Type playerChat = message.getTypes().getOrDefault(fPlayer.getSettingValue(FPlayer.Setting.CHAT), getPlayerChat(fPlayer, eventMessage));

        var configChatEntry = message.getTypes().entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(playerChat))
                .findAny();

        if (playerChat == null || !playerChat.isEnable() || configChatEntry.isEmpty()) {
            builder(fPlayer)
                    .format(Localization.Message.Chat::getNullChat)
                    .sendBuilt();
            event.getRecipients().clear();
            event.setCancelled(true);
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
                event.getRecipients().clear();
                event.setCancelled(true);
                return;
            }
        }

        String trigger = playerChat.getTrigger();

        if (trigger != null && !trigger.isEmpty() && eventMessage.startsWith(trigger)) {
            eventMessage = eventMessage.substring(trigger.length()).trim();
        }

        Player sender = Bukkit.getPlayer(fPlayer.getUuid());
        if (sender == null) return;

        Predicate<FPlayer> chatPermissionFilter = fReceiver -> permissionChecker.check(fReceiver, permission.getTypes().get(chatName));

        int chatRange = playerChat.getRange();

        // in local chat you can mention it too,
        // but I don't want to full support InteractiveChat
        String finalMessage = chatRange == Range.PROXY
                || chatRange == Range.SERVER
                || chatRange == Range.WORLD_NAME
                || chatRange == Range.WORLD_TYPE
                ? integrationModule.checkMention(fPlayer, eventMessage)
                : eventMessage;

        Builder builder = builder(fPlayer)
                .tag(MessageTag.CHAT)
                .destination(playerChat.getDestination())
                .range(chatRange)
                .filter(chatPermissionFilter)
                .format(message -> message.getTypes().get(chatName))
                .message(finalMessage)
                .proxy(output -> {
                    output.writeUTF(chatName);
                    output.writeUTF(finalMessage);
                })
                .integration(s -> s.replace("<message>", finalMessage))
                .sound(soundMap.get(chatName));

        List<FPlayer> receivers = builder.build();

        builder.send(receivers);

        List<UUID> receiversUUID = receivers.stream()
                .filter(filterFPlayer -> !filterFPlayer.isUnknown())
                .map(FEntity::getUuid)
                .toList();

        spyModuleProvider.get().checkChat(fPlayer, chatName, finalMessage);

        int countReceivers = receiversUUID.size();
        Message.Chat.Type.NullReceiver nullReceiver = playerChat.getNullReceiver();

        if (nullReceiver.isEnable() && countReceivers < 2) {
            checkReceiversLater(fPlayer, countReceivers, chatRange, receiversUUID, nullReceiver);
        }

        event.setMessage(finalMessage);
        event.setCancelled(playerChat.isCancel());
        event.getRecipients().clear();

        bubbleModuleProvider.get().add(fPlayer, eventMessage);
    }

    @Async(delay = 5L)
    public void checkReceiversLater(FPlayer fPlayer, int countReceiver, int chatRange,
                                    List<UUID> receiversUUID, Message.Chat.Type.NullReceiver nullReceiver) {
        Set<UUID> onlinePlayers = fPlayerService.findOnlineFPlayers()
                .stream()
                .map(FEntity::getUuid)
                .collect(Collectors.toSet());

        if ((onlinePlayers.containsAll(receiversUUID) && onlinePlayers.size() <= countReceiver)
                || chatRange > -1) {
            builder(fPlayer)
                    .destination(nullReceiver.getDestination())
                    .format(Localization.Message.Chat::getNullReceiver)
                    .sendBuilt();
        }
    }

    public void send(FEntity fPlayer, String chatName, String string) {
        if (checkModulePredicates(fPlayer)) return;

        var optionalChat = message.getTypes().entrySet().stream()
                .filter(chat -> chat.getKey().equals(chatName))
                .findAny();

        if (optionalChat.isEmpty()) return;

        String playerChatName = optionalChat.get().getKey();
        Message.Chat.Type playerChat = optionalChat.get().getValue();

        Predicate<FPlayer> filter = rangeFilter(fPlayer, playerChat.getRange()).and(fReceiver -> {
            if (!permissionChecker.check(fReceiver, permission.getTypes().get(playerChatName))) return false;

            return Bukkit.getPlayer(fReceiver.getUuid()) != null;
        });

        builder(fPlayer)
                .range(Range.SERVER)
                .destination(playerChat.getDestination())
                .filter(filter)
                .format(s -> s.getTypes().get(playerChatName))
                .message(string)
                .sound(getSound())
                .sendBuilt();
    }
}
