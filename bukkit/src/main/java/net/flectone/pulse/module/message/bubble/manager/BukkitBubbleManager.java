package net.flectone.pulse.module.message.bubble.manager;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.model.FPacketEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.bubble.model.FBubble;
import net.flectone.pulse.converter.ColorConverter;
import net.flectone.pulse.pipeline.MessagePipeline;
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

    private final TaskScheduler taskScheduler;
    private final FPlayerService fPlayerService;
    private final FileManager fileManager;
    private final MessagePipeline messagePipeline;
    private final RandomUtil randomUtil;
    private final ColorConverter colorConverter;
    private final IntegrationModule integrationModule;

    @Inject
    public BukkitBubbleManager(TaskScheduler taskScheduler,
                               FPlayerService fPlayerService,
                               FileManager fileManager,
                               RandomUtil randomUtil,
                               MessagePipeline messagePipeline,
                               ColorConverter colorConverter,
                               IntegrationModule integrationModule) {
        this.taskScheduler = taskScheduler;
        this.fPlayerService = fPlayerService;
        this.fileManager = fileManager;
        this.randomUtil = randomUtil;
        this.messagePipeline = messagePipeline;
        this.colorConverter = colorConverter;
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

        taskScheduler.runAsyncLater(() -> process(fPlayer), duration);
    }

    public long next(FPlayer fPlayer, String message) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return -1;

        Message.Bubble messageBubble = fileManager.getMessage().getBubble();
        Message.Bubble.Modern modernBubble = messageBubble.getModern();
        Localization.Message.Bubble localizationBubble = fileManager.getLocalization(fPlayer).getMessage().getBubble();

        boolean isTextDisplay = PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_4) && modernBubble.isEnable();

        // 1.21.3+ supported interaction riding
        boolean isInteractionRiding = modernBubble.isInteractionRiding() && PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_21_3);

        int lineWidth = messageBubble.getLineWidth();
        double distance = messageBubble.getDistance();
        long duration = calculateDuration(message, messageBubble);

        boolean hasShadow = modernBubble.isHasShadow();
        int background = isTextDisplay ? colorConverter.parseHexToArgb(modernBubble.getBackground()) : 0;
        float scale = modernBubble.getScale();
        float height = modernBubble.getHeight();

        player.getWorld().getPlayers()
                .stream()
                .filter(receiver -> receiver.getWorld().equals(player.getWorld()))
                .filter(receiver -> receiver.canSee(player))
                .filter(receiver -> receiver.getLocation().distance(player.getLocation()) <= distance)
                .forEach(receiver -> {
                    FPlayer fReceiver = fPlayerService.getFPlayer(receiver);
                    if (fReceiver.isIgnored(fPlayer)) return;

                    Component component = messagePipeline.builder(fPlayer, fReceiver, localizationBubble.getFormat())
                            .mention(false)
                            .question(false)
                            .interactiveChat(false)
                            .build()
                            .replaceText(TextReplacementConfig.builder().match("<message>").replacement(
                                                    messagePipeline.builder(fPlayer, fReceiver, message)
                                                            .userMessage(true)
                                                            .mention(false)
                                                            .question(false)
                                                            .interactiveChat(false)
                                                            .build()
                                            )
                                            .build()
                            );

                    if (isTextDisplay) {

                        ArrayDeque<FBubble> fBubbles = new ArrayDeque<>();

                        if (isInteractionRiding) {
                            fBubbles.add(new FBubble(duration, height, fPlayer, fReceiver));
                        } else {
                            for (int i = 0; i < (int) height; i++) {
                                fBubbles.add(new FBubble(duration, fPlayer, fReceiver, Component.text(""), false));
                            }
                        }

                        fBubbles.add(new FBubble(hasShadow, duration, lineWidth, background, scale, fPlayer, fReceiver, component));

                        teleport(fBubbles);

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
                || integrationModule.isVanished(fPlayerService.getFPlayer(player.getUniqueId()))) {
            return;
        }

        int lastID = player.getEntityId();

        for (FBubble fBubble : bubbleDeque) {
            fBubble.spawn(randomUtil);
            fBubbleList.add(fBubble);
            taskScheduler.runAsyncLater(fBubble::remove, fBubble.getDuration());

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
