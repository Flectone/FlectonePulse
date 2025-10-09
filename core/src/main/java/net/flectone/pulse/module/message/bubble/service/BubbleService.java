package net.flectone.pulse.module.message.bubble.service;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.bubble.model.Bubble;
import net.flectone.pulse.module.message.bubble.model.ModernBubble;
import net.flectone.pulse.module.message.bubble.renderer.BubbleRenderer;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.processing.converter.ColorConverter;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.constant.MessageFlag;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Singleton
public class BubbleService {

    private final Map<UUID, Queue<Bubble>> playerBubbleQueues = new ConcurrentHashMap<>();

    private final FileResolver fileResolver;
    private final BubbleRenderer bubbleRenderer;
    private final ColorConverter colorConverter;
    private final TaskScheduler taskScheduler;
    private final PacketProvider packetProvider;
    private final RandomUtil randomUtil;
    private final MessagePipeline messagePipeline;
    
    @Inject
    public BubbleService(FileResolver fileResolver,
                         BubbleRenderer bubbleRenderer,
                         ColorConverter colorConverter,
                         TaskScheduler taskScheduler,
                         PacketProvider packetProvider,
                         RandomUtil randomUtil,
                         MessagePipeline messagePipeline) {
        this.fileResolver = fileResolver;
        this.bubbleRenderer = bubbleRenderer;
        this.colorConverter = colorConverter;
        this.taskScheduler = taskScheduler;
        this.packetProvider = packetProvider;
        this.randomUtil = randomUtil;
        this.messagePipeline = messagePipeline;
    }

    public void startTicker() {
        taskScheduler.runAsyncTimer(() -> playerBubbleQueues.forEach(this::processBubbleQueue), 5L, 5L);
    }

    public void addMessage(@NotNull FPlayer sender, @NotNull String message, List<FPlayer> receivers) {
        if (!bubbleRenderer.isCorrectPlayer(sender)) return;

        Queue<Bubble> bubbleQueue = playerBubbleQueues.computeIfAbsent(
                sender.getUuid(), 
                uuid -> new ConcurrentLinkedQueue<>()
        );

        message = messagePipeline.builder(sender, message)
                .flag(MessageFlag.USER_MESSAGE, true)
                .flag(MessageFlag.MENTION, false)
                .flag(MessageFlag.INTERACTIVE_CHAT, false)
                .flag(MessageFlag.QUESTION, false)
                .flag(MessageFlag.TRANSLATE_ITEM, false)
                .flag(MessageFlag.OBJECT, false)
                .flag(MessageFlag.REPLACE_DISABLED_TAGS, false)
                .plainSerializerBuild();

        List<Bubble> bubbles = splitMessageToBubbles(sender, message, receivers);

        bubbleQueue.addAll(bubbles);
    }

    private List<Bubble> splitMessageToBubbles(@NotNull FPlayer sender, @NotNull String message, List<FPlayer> receivers) {
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
        BubbleModule.Billboard billboard = configModern.getBillboard();

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
                    animationTime, scale, billboard, receivers
            ));

            line.setLength(0);
        }

        if (!line.isEmpty()) {
            bubbles.add(buildBubble(
                    id, sender, line.toString(), duration, elevation, interactionHeight,
                    useInteractionRiding, useModernBubble, hasShadow, background,
                    animationTime, scale, billboard, receivers
            ));
        }

        return bubbles;
    }

    private Bubble buildBubble(int id, FPlayer sender, String message, long duration, int elevation, float interactionHeight,
                               boolean interactionRiding, boolean useModern, boolean hasShadow, int background,
                               int animationTime, float scale, BubbleModule.Billboard billboard, List<FPlayer> receivers) {
        Bubble.BubbleBuilder<?, ?> builder = useModern
                ? ModernBubble.builder()
                .hasShadow(hasShadow)
                .background(background)
                .animationTime(animationTime)
                .scale(scale)
                .billboard(billboard)
                .viewers(receivers)
                : Bubble.builder();

        return builder
                .id(id)
                .sender(sender)
                .rawMessage(message)
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

    public void clear() {
        playerBubbleQueues.clear();
        bubbleRenderer.removeAllBubbles();
    }
    
    private long calculateDuration(String message) {
        Message.Bubble config = fileResolver.getMessage().getBubble();

        int countWords = message.split(" ").length;
        return (long) (((countWords + config.getHandicapChars()) / config.getReadSpeed()) * 60) * 1000L;
    }

    private boolean isModern() {
        return packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19_4)
                && fileResolver.getMessage().getBubble().getModern().isEnable();
    }

    private boolean isInteractionRiding() {
        return packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_3)
                && fileResolver.getMessage().getBubble().getInteraction().isEnable();
    }
}