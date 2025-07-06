package net.flectone.pulse.module.message.bubble.service;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.converter.ColorConverter;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.bubble.model.Bubble;
import net.flectone.pulse.module.message.bubble.model.ModernBubble;
import net.flectone.pulse.module.message.bubble.renderer.BubbleRenderer;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.util.RandomUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class BubbleService {

    private final Map<UUID, Queue<Bubble>> playerBubbleQueues = new ConcurrentHashMap<>();

    private final FileResolver fileResolver;
    private final BubbleRenderer bubbleRenderer;
    private final ColorConverter colorConverter;
    private final TaskScheduler taskScheduler;
    private final RandomUtil randomUtil;
    private final MessagePipeline messagePipeline;
    
    @Inject
    public BubbleService(TaskScheduler taskScheduler,
                         FileResolver fileResolver,
                         BubbleRenderer bubbleRenderer,
                         ColorConverter colorConverter,
                         RandomUtil randomUtil,
                         MessagePipeline messagePipeline) {
        this.fileResolver = fileResolver;
        this.bubbleRenderer = bubbleRenderer;
        this.colorConverter = colorConverter;
        this.taskScheduler = taskScheduler;
        this.randomUtil = randomUtil;
        this.messagePipeline = messagePipeline;
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

        message = messagePipeline.builder(sender, message)
                .userMessage(true)
                .mention(false)
                .question(false)
                .interactiveChat(false)
                .translateItem(false)
                .plainSerializerBuild();

        List<Bubble> bubbles = splitMessageToBubbles(sender, message);

        bubbleQueue.addAll(bubbles);
    }

    private List<Bubble> splitMessageToBubbles(@NotNull FPlayer sender, @NotNull String message) {
        int id = randomUtil.nextInt(Integer.MAX_VALUE);

        // default bubble
        Message.Bubble config = fileResolver.getMessage().getBubble();

        long duration = calculateDuration(message);
        int elevation = config.getElevation();
        float interactionHeight = config.getInteraction().getHeight();

        boolean useModernBubble = isModern();
        boolean useInteractionRiding = isInteractionRiding();

        String wordBreakHint = config.getWordBreakHint();

        // modern bubble
        Message.Bubble.Modern configModern = config.getModern();

        boolean hasShadow = configModern.isHasShadow();
        int background = colorConverter.parseHexToArgb(configModern.getBackground());
        int animationTime = configModern.getAnimationTime();
        float scale = configModern.getScale();
        Message.Bubble.Billboard billboard = configModern.getBillboard();

        int maxLength = fileResolver.getMessage().getBubble().getMaxLength();

        List<Bubble> bubbles = new ArrayList<>();

        StringBuilder line = new StringBuilder();
        for (char symbol : message.toCharArray()) {
            line.append(symbol);
            if (line.length() < maxLength) continue;

            boolean isLetter = Character.isLetter(symbol);
            if (!isLetter && line.length() < maxLength + 5) continue;

            String newMessage = isLetter ? line + wordBreakHint : line.toString().trim();
            bubbles.add(buildBubble(
                    id, sender, newMessage, duration, elevation, interactionHeight,
                    useInteractionRiding, useModernBubble, hasShadow, background,
                    animationTime, scale, billboard
            ));

            line.setLength(0);
        }

        if (!line.isEmpty()) {
            bubbles.add(buildBubble(
                    id, sender, line.toString(), duration, elevation, interactionHeight,
                    useInteractionRiding, useModernBubble, hasShadow, background,
                    animationTime, scale, billboard
            ));
        }

        return bubbles;
    }

    private Bubble buildBubble(int id, FPlayer sender, String message, long duration, int elevation, float interactionHeight,
                               boolean interactionRiding, boolean useModern, boolean hasShadow, int background,
                               int animationTime, float scale, Message.Bubble.Billboard billboard) {
        Bubble.Builder builder = useModern
                ? new ModernBubble.ModernBuilder()
                .hasShadow(hasShadow)
                .background(background)
                .animationTime(animationTime)
                .scale(scale)
                .billboard(billboard)
                : new Bubble.Builder();

        return builder
                .id(id)
                .sender(sender)
                .message(message)
                .duration(duration)
                .elevation(elevation)
                .interactionHeight(interactionHeight)
                .interactionRiding(interactionRiding)
                .build();
    }

    private void processBubbleQueue(UUID playerUuid, Queue<Bubble> bubbleQueue) {
        if (bubbleQueue == null || bubbleQueue.isEmpty()) {
            playerBubbleQueues.remove(playerUuid);
            return;
        }

        int maxCount = fileResolver.getMessage().getBubble().getMaxCount();

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
        Message.Bubble config = fileResolver.getMessage().getBubble();

        int countWords = message.split(" ").length;
        return (long) (((countWords + config.getHandicapChars()) / config.getReadSpeed()) * 60) * 1000L;
    }

    private boolean isModern() {
        return PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_4)
                && fileResolver.getMessage().getBubble().getModern().isEnable();
    }

    private boolean isInteractionRiding() {
        return PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_21_3)
                && fileResolver.getMessage().getBubble().getInteraction().isEnable();
    }
}