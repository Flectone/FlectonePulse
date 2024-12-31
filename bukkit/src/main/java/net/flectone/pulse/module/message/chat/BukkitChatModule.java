package net.flectone.pulse.module.message.chat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.manager.BukkitListenerManager;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.bubble.BukkitBubbleModule;
import net.flectone.pulse.module.message.chat.listener.ChatListener;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.PermissionUtil;
import net.flectone.pulse.util.Range;
import net.flectone.pulse.util.TimeUtil;
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

    private final FPlayerManager fPlayerManager;
    private final PermissionUtil permissionUtil;
    private final ThreadManager threadManager;
    private final BukkitListenerManager bukkitListenerManager;
    private final IntegrationModule integrationModule;
    private final TimeUtil timeUtil;

    @Inject
    private BukkitBubbleModule bubbleModule;

    @Inject
    public BukkitChatModule(FileManager fileManager,
                            FPlayerManager fPlayerManager,
                            ThreadManager threadManager,
                            BukkitListenerManager bukkitListenerManager,
                            PermissionUtil permissionUtil,
                            IntegrationModule integrationModule,
                            TimeUtil timeUtil) {
        super(fileManager);

        this.fPlayerManager = fPlayerManager;
        this.permissionUtil = permissionUtil;
        this.threadManager = threadManager;
        this.bukkitListenerManager = bukkitListenerManager;
        this.integrationModule = integrationModule;
        this.timeUtil = timeUtil;
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

        String string = integrationModule.checkMention(fPlayer, event);

        Message.Chat.Type playerChat = message.getTypes().getOrDefault(fPlayer.getChat(), getPlayerChat(fPlayer, string));

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
            if (cooldownMap.get(chatName).isCooldowned(fPlayer, permissionUtil)) {
                long timeLeft = cooldownMap.get(chatName).getTimeLeft(fPlayer);

                builder(fPlayer)
                        .format(timeUtil.format(fPlayer, timeLeft, getCooldownMessage(fPlayer)))
                        .sendBuilt();
                event.getRecipients().clear();
                event.setCancelled(true);
                return;
            }
        }

        String trigger = playerChat.getTrigger();

        if (trigger != null && !trigger.isEmpty() && string.startsWith(trigger)) {
            string = string.substring(trigger.length()).trim();
        }

        Player sender = Bukkit.getPlayer(fPlayer.getUuid());
        if (sender == null) return;

        Predicate<FPlayer> filter = rangeFilter(fPlayer, playerChat.getRange()).and(fReceiver -> {
            if (!permissionUtil.has(fReceiver, permission.getTypes().get(chatName))) return false;

            Player receiver = Bukkit.getPlayer(fReceiver.getUuid());
            if (receiver == null) return true;

            if (playerChat.getRange() != Range.PROXY && playerChat.getRange() != Range.SERVER) {

                if (!receiver.getWorld().equals(sender.getWorld())) return false;
                return !(receiver.getLocation().distance(sender.getLocation()) > playerChat.getRange());
            }

            return true;
        });

        String finalMessage = string;
        threadManager.runAsync(database -> {

            int chatRange = playerChat.getRange();

            builder(fPlayer)
                    .tag(MessageTag.CHAT)
                    .range(chatRange)
                    .filter(filter)
                    .format(message -> message.getTypes().get(chatName))
                    .message(finalMessage)
                    .proxy(output -> {
                        output.writeUTF(chatName);
                        output.writeUTF(finalMessage);
                    })
                    .integration(s -> s.replace("<message>", finalMessage))
                    .sound(soundMap.get(chatName))
                    .sendBuilt();

            List<UUID> recipients = fPlayerManager.getFPlayers()
                    .stream()
                    .filter(filter)
                    .map(FEntity::getUuid)
                    .toList();

            int countRecipients = recipients.size();

            if (playerChat.isNullRecipient() && countRecipients < 2) {

                Set<UUID> onlinePlayers = database.getOnlineFPlayers()
                        .stream()
                        .map(FEntity::getUuid)
                        .collect(Collectors.toSet());

                if ((onlinePlayers.containsAll(recipients) && onlinePlayers.size() <= countRecipients)
                        || chatRange > -1) {
                    threadManager.runAsyncLater(() -> builder(fPlayer)
                            .format(Localization.Message.Chat::getNullRecipient)
                            .sendBuilt(), 5);
                }
            }
        });

        event.setCancelled(playerChat.isCancel());
        event.getRecipients().clear();

        bubbleModule.add(fPlayer, string);
    }

    @Async
    public void send(FEntity fPlayer, String chatName, String string) {
        if (checkModulePredicates(fPlayer)) return;

        var optionalChat = message.getTypes().entrySet().stream()
                .filter(chat -> chat.getKey().equals(chatName))
                .findAny();

        if (optionalChat.isEmpty()) return;

        String playerChatName = optionalChat.get().getKey();
        Message.Chat.Type playerChat = optionalChat.get().getValue();

        Predicate<FPlayer> filter = rangeFilter(fPlayer, playerChat.getRange()).and(fReceiver -> {
            if (!permissionUtil.has(fReceiver, permission.getTypes().get(playerChatName))) return false;

            return Bukkit.getPlayer(fReceiver.getUuid()) != null;
        });

        builder(fPlayer)
                .filter(filter)
                .format(s -> s.getTypes().get(playerChatName))
                .message(string)
                .sound(getSound())
                .sendBuilt();
    }
}
