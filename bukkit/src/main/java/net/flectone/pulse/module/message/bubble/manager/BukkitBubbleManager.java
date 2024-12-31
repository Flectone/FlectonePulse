package net.flectone.pulse.module.message.bubble.manager;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FPacketEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.bubble.model.FBubble;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.RandomUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class BukkitBubbleManager implements BubbleManager {

    public final Map<UUID, Queue<String>> messageMap = new ConcurrentHashMap<>();
    private final List<FBubble> fBubbleList = new ArrayList<>();

    private final ThreadManager threadManager;
    private final FPlayerManager fPlayerManager;
    private final FileManager fileManager;
    private final ComponentUtil componentUtil;
    private final RandomUtil randomUtil;
    private final IntegrationModule integrationModule;

    @Inject
    public BukkitBubbleManager(ThreadManager threadManager,
                               FPlayerManager fPlayerManager,
                               FileManager fileManager,
                               RandomUtil randomUtil,
                               ComponentUtil componentUtil,
                               IntegrationModule integrationModule) {
        this.threadManager = threadManager;
        this.fPlayerManager = fPlayerManager;
        this.fileManager = fileManager;
        this.randomUtil = randomUtil;
        this.componentUtil = componentUtil;
        this.integrationModule = integrationModule;
    }

    public void add(FPlayer fPlayer, String message) {
        Queue<String> queue = messageMap.get(fPlayer.getUuid());

        boolean newMessage = false;
        if (queue == null) {
            queue = new LinkedList<>();
            newMessage = true;
        }

        queue.add(message);
        messageMap.put(fPlayer.getUuid(), queue);

        if (newMessage) {
            process(fPlayer);
        }
    }

    @Async
    @Override
    public void process(FPlayer fPlayer) {
        Queue<String> queue = messageMap.get(fPlayer.getUuid());
        if (queue == null) return;
        if (queue.isEmpty()) {
            messageMap.remove(fPlayer.getUuid());
            return;
        }

        String message = queue.poll();
        long duration = next(fPlayer, message);
        if (duration == -1) {
            messageMap.remove(fPlayer.getUuid());
            return;
        }

        threadManager.runAsyncLater(() -> process(fPlayer), duration);
    }

    public long next(FPlayer fPlayer, String message) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return -1;

        Message.Bubble messageBubble = fileManager.getMessage().getBubble();
        Localization.Message.Bubble localizationBubble = fileManager.getLocalization(fPlayer).getMessage().getBubble();

        boolean isTextDisplay = PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_4) && messageBubble.isNewSystem();

        int lineWidth = messageBubble.getLineWidth();
        float height = messageBubble.getHeight();

        long duration = calculateDuration(message, messageBubble);

        player.getWorld().getPlayers().forEach(receiver -> {
            FPlayer fReceiver = fPlayerManager.get(receiver);
            if (fReceiver.isIgnored(fPlayer)) return;

            Component component = componentUtil.builder(fPlayer, fReceiver, localizationBubble.getFormat())
                    .build()
                    .replaceText(TextReplacementConfig.builder().match("<message>").replacement(
                            componentUtil.builder(fPlayer, fReceiver, message)
                                    .userMessage(true)
                                    .build()
                            )
                            .build()
                    );

            if (isTextDisplay) {
                teleport(new ArrayDeque<>(List.of(
                        new FBubble(duration, height, fPlayer, fReceiver),
                        new FBubble(duration, lineWidth, fPlayer, fReceiver, component)
                )));
                return;
            }

            teleport(
                    divideText(component, lineWidth / 4)
                    .stream()
                    .map(string -> {
                        Component componentString = LegacyComponentSerializer.legacySection().deserialize(string).style(component.style());
                        return new FBubble(duration, fPlayer, fReceiver, componentString);
                    })
                    .collect(Collectors.toCollection(ArrayDeque::new))
            );
        });

        return duration;
    }

    @Sync
    public void teleport(Deque<FBubble> bubbleDeque) {

        Player player = Bukkit.getPlayer(bubbleDeque.getFirst().getFPlayer().getUuid());
        if (player == null) return;

        if (player.getGameMode() == GameMode.SPECTATOR
                || player.hasPotionEffect(PotionEffectType.INVISIBILITY)
                || !player.getPassengers().isEmpty()
                || integrationModule.isVanished(fPlayerManager.get(player.getUniqueId()))) {
            return;
        }

        int lastID = player.getEntityId();

        for (FBubble fBubble : bubbleDeque) {
            fBubble.spawn(randomUtil);
            fBubbleList.add(fBubble);
            threadManager.runAsyncLater(fBubble::remove, fBubble.getDuration());

            Player sender = Bukkit.getPlayer(fBubble.getFPlayer().getUuid());
            if (sender == null) continue;

            Player receiver = Bukkit.getPlayer(fBubble.getFReceiver().getUuid());
            if (receiver == null) return;

            List<Integer> passengers = sender.getPassengers().stream().map(Entity::getEntityId).collect(Collectors.toList());
            passengers.add(fBubble.getId());

            int[] passengersIds = passengers.stream().mapToInt(Number::intValue).toArray();

            PacketEvents.getAPI().getPlayerManager().sendPacket(receiver, new WrapperPlayServerSetPassengers(lastID, passengersIds));

            lastID = fBubble.getId();
        }
    }

    private Deque<String> divideText(Component component, int lineWidth) {
        String text = PlainTextComponentSerializer.plainText().serialize(component);

        Deque<String> lines = new ArrayDeque<>();
        StringBuilder line = new StringBuilder();

        for (char symbol : text.toCharArray()) {
            line.append(symbol);

            if ((symbol == ' ' && line.length() > lineWidth - 5)
                    || line.length() > lineWidth) {

                lines.push(symbol == ' ' ? line.toString().trim() : line + "-");
                line = new StringBuilder();
            }
        }

        if (!line.isEmpty()) {
            lines.push(line.toString());
        }

        return lines;
    }

    @Override
    public void remove(FPlayer fPlayer) {
        messageMap.remove(fPlayer.getUuid());
        fBubbleList.stream()
                .filter(fBubble -> fBubble.getFPlayer().equals(fPlayer))
                .filter(FPacketEntity::isAlive)
                .forEach(FBubble::remove);
    }

    @Override
    public void reload() {
        messageMap.clear();
        fBubbleList.stream()
                .filter(FPacketEntity::isAlive)
                .forEach(FBubble::remove);
        fBubbleList.clear();
    }

    private long calculateDuration(String message, Message.Bubble config) {
        int countWords = message.split(" ").length;
        return (long) (((countWords + config.getHandicapChars()) / config.getReadSpeed()) * 1200L);
    }
}
