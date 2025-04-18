package net.flectone.pulse.module.message.bubble.service;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.converter.ColorConverter;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.bubble.model.Bubble;
import net.flectone.pulse.module.message.bubble.model.ModernBubble;
import net.flectone.pulse.module.message.bubble.renderer.BubbleRenderer;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.util.RandomUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class BubbleService {

    private final Map<UUID, Queue<Bubble>> playerBubbleQueues = new ConcurrentHashMap<>();

    private final FileManager fileManager;
    private final BubbleRenderer bubbleRenderer;
    private final ColorConverter colorConverter;
    private final TaskScheduler taskScheduler;
    private final RandomUtil randomUtil;
    
    @Inject
    public BubbleService(TaskScheduler taskScheduler,
                         FileManager fileManager,
                         BubbleRenderer bubbleRenderer,
                         ColorConverter colorConverter,
                         RandomUtil randomUtil) {
        this.fileManager = fileManager;
        this.bubbleRenderer = bubbleRenderer;
        this.colorConverter = colorConverter;
        this.taskScheduler = taskScheduler;
        this.randomUtil = randomUtil;
    }

    private void startTicker() {
        taskScheduler.runAsyncTimer(() -> playerBubbleQueues.forEach(this::processBubbleQueue), 5L, 5L);
    }

    public void addMessage(@NotNull FPlayer sender, @NotNull String message) {
        if (!bubbleRenderer.isCorrectPlayer(sender)) return;

        Queue<Bubble> bubbleQueue = playerBubbleQueues.computeIfAbsent(
                sender.getUuid(), 
                uuid -> new LinkedList<>()
        );
        
        List<Bubble> bubbles = splitMessageToBubbles(sender, message);

        bubbleQueue.addAll(bubbles);
    }

    private List<Bubble> splitMessageToBubbles(@NotNull FPlayer sender, @NotNull String message) {
        int id = randomUtil.nextInt(Integer.MAX_VALUE);

        Message.Bubble config = fileManager.getMessage().getBubble();

        long duration = calculateDuration(message);
        float height = config.getHeight();

        boolean useModernBubble = isModern();
        boolean useInteractionRiding = isInteractionRiding();
        boolean hasShadow = config.getModern().isHasShadow();
        int background = colorConverter.parseHexToArgb(config.getModern().getBackground());
        float scale = config.getModern().getScale();

        int maxLength = fileManager.getMessage().getBubble().getMaxLength();

        List<Bubble> bubbles = new ArrayList<>();

        StringBuilder line = new StringBuilder();
        for (char symbol : message.toCharArray()) {
            line.append(symbol);

            if ((symbol == ' ' && line.length() > maxLength - 5) || line.length() > maxLength) {

                String newMessage = symbol == ' ' ? line.toString().trim() : line + "-";

                Bubble bubble = useModernBubble
                        ? new ModernBubble(id, sender, newMessage, duration, height, useInteractionRiding, hasShadow, background, scale)
                        : new Bubble(id, sender, newMessage, duration, height, useInteractionRiding);

                bubbles.add(bubble);

                line = new StringBuilder();
            }
        }

        if (!line.isEmpty()) {
            Bubble bubble = useModernBubble
                    ? new ModernBubble(id, sender, line.toString(), duration, height, useInteractionRiding, hasShadow, background, scale)
                    : new Bubble(id, sender, line.toString(), duration, height, useInteractionRiding);

            bubbles.add(bubble);
        }

        return bubbles;
    }

    private void processBubbleQueue(UUID playerUuid, Queue<Bubble> bubbleQueue) {
        if (bubbleQueue == null || bubbleQueue.isEmpty()) {
            playerBubbleQueues.remove(playerUuid);
            return;
        }

        int maxCount = fileManager.getMessage().getBubble().getMaxCount();

        bubbleQueue.removeIf(bubble -> {
            if (bubble.isExpired()) {
                bubbleRenderer.removeBubble(bubble);
                return true;
            }

            return false;
        });

        bubbleQueue.stream()
                .limit(maxCount)
                .filter(bubble -> !bubble.isCreated())
                .forEach(bubbleRenderer::renderBubble);
    }

    public void clear(FPlayer fPlayer) {
        Queue<Bubble> bubbleQueue = playerBubbleQueues.get(fPlayer.getUuid());
        if (bubbleQueue == null) return;

        bubbleQueue.forEach(bubbleRenderer::removeBubble);
        playerBubbleQueues.remove(fPlayer.getUuid());
    }

    public void reload() {
        playerBubbleQueues.clear();
        bubbleRenderer.removeAllBubbles();
        startTicker();
    }
    
    private long calculateDuration(String message) {
        Message.Bubble config = fileManager.getMessage().getBubble();

        int countWords = message.split(" ").length;
        return (long) (((countWords + config.getHandicapChars()) / config.getReadSpeed()) * 60) * 1000L;
    }

    private boolean isModern() {
        return PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_4)
                && fileManager.getMessage().getBubble().getModern().isEnable();
    }

    private boolean isInteractionRiding() {
        return PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_21_3)
                && fileManager.getMessage().getBubble().isUseInteraction();
    }
}