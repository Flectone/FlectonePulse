package net.flectone.pulse.module.message.bubble.service;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.bubble.model.Bubble;
import net.flectone.pulse.module.message.bubble.model.ModernBubble;
import net.flectone.pulse.module.message.bubble.renderer.BubbleRenderer;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.converter.ColorConverter;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BubbleService {

    private static final Map<MessageFlag, Boolean> PLAIN_MESSAGE_FLAGS = Map.of(MessageFlag.USER_MESSAGE, true,
            MessageFlag.MENTION, false,
            MessageFlag.INTERACTIVE_CHAT, false,
            MessageFlag.QUESTION, false,
            MessageFlag.TRANSLATE_ITEM, false,
            MessageFlag.OBJECT_SPRITE, false,
            MessageFlag.OBJECT_PLAYER_HEAD, false,
            MessageFlag.REPLACE_DISABLED_TAGS, false
    );

    private final Map<UUID, PlayerBubbleState> playerBubbleStates = new ConcurrentHashMap<>();

    private record PlayerBubbleState(
            Queue<Bubble> waitingQueue,
            Queue<Bubble> activeBubbles,
            ReentrantLock lock
    ) {}

    private final FileFacade fileFacade;
    private final BubbleRenderer bubbleRenderer;
    private final ColorConverter colorConverter;
    private final TaskScheduler taskScheduler;
    private final PacketProvider packetProvider;
    private final RandomUtil randomUtil;
    private final MessagePipeline messagePipeline;

    public void startTicker() {
        taskScheduler.runPlayerRegionTimer(fPlayer -> {
            PlayerBubbleState state = playerBubbleStates.get(fPlayer.getUuid());
            if (state == null) return;

            processBubbleQueue(fPlayer.getUuid(), state);
        }, 1L);
    }

    public void addMessage(@NonNull FPlayer sender, @NonNull String message, List<FPlayer> receivers) {
        if (!bubbleRenderer.isCorrectPlayer(sender)) return;

        PlayerBubbleState state = playerBubbleStates.computeIfAbsent(
                sender.getUuid(),
                uuid -> new PlayerBubbleState(new ConcurrentLinkedQueue<>(), new ConcurrentLinkedQueue<>(), new ReentrantLock())
        );

        MessageContext messageContext = messagePipeline.createContext(sender, message).withFlags(PLAIN_MESSAGE_FLAGS);
        List<Bubble> bubbles = splitMessageToBubbles(sender, messagePipeline.buildPlain(messageContext), receivers);

        state.waitingQueue.addAll(bubbles);
    }

    private List<Bubble> splitMessageToBubbles(@NonNull FPlayer sender, @NonNull String message, List<FPlayer> receivers) {
        int id = randomUtil.nextInt(Integer.MAX_VALUE);

        // default bubble
        Message.Bubble config = fileFacade.message().bubble();

        long duration = calculateDuration(message);
        float elevation = config.elevation();
        float interactionHeight = config.interaction().height();

        boolean useModernBubble = isModern();
        boolean useInteractionRiding = isInteractionRiding();

        String wordBreakHint = config.wordBreakHint();

        // modern bubble
        Message.Bubble.Modern configModern = config.modern();

        boolean hasShadow = configModern.hasShadow();
        boolean seeThrough = configModern.seeThrough();
        int background = colorConverter.parseHexToArgb(configModern.background());
        int animationTime = configModern.animationTime();
        float scale = configModern.scale();
        BubbleModule.Billboard billboard = configModern.billboard();

        int maxLength = fileFacade.message().bubble().maxLength();

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
                    useInteractionRiding, useModernBubble, hasShadow, seeThrough, background,
                    animationTime, scale, billboard, receivers
            ));

            line.setLength(0);
        }

        if (!line.isEmpty()) {
            bubbles.add(buildBubble(
                    id, sender, line.toString(), duration, elevation, interactionHeight,
                    useInteractionRiding, useModernBubble, hasShadow, seeThrough, background,
                    animationTime, scale, billboard, receivers
            ));
        }

        return bubbles;
    }

    private Bubble buildBubble(int id, FPlayer sender, String message, long duration, float elevation, float interactionHeight,
                               boolean interactionRiding, boolean useModern, boolean hasShadow, boolean seeThrough, int background,
                               int animationTime, float scale, BubbleModule.Billboard billboard, List<FPlayer> receivers) {
        Bubble.BubbleBuilder<?, ?> builder = useModern
                ? ModernBubble.builder()
                .hasShadow(hasShadow)
                .seeThrough(seeThrough)
                .background(background)
                .animationTime(animationTime)
                .scale(scale)
                .billboard(billboard)
                : Bubble.builder();

        return builder
                .id(id)
                .sender(sender)
                .rawMessage(message)
                .duration(duration)
                .elevation(elevation)
                .interactionHeight(interactionHeight)
                .interactionRiding(interactionRiding)
                .viewers(receivers)
                .build();
    }

    private void processBubbleQueue(UUID playerUuid, PlayerBubbleState bubbleState) {
        if (!bubbleState.lock.tryLock()) return;

        try {
            bubbleState.activeBubbles.removeIf(bubble -> {
                if (!bubble.isExpired()) return false;
                bubbleRenderer.removeBubbleIf(bubbleEntity -> bubbleEntity.getBubble().getId() == bubble.getId());
                return true;
            });

            int maxCount = fileFacade.message().bubble().maxCount();
            if (bubbleState.activeBubbles.size() >= maxCount) {
                return;
            }

            Bubble nextBubble = bubbleState.waitingQueue.poll();
            if (nextBubble != null && !nextBubble.isCreated()) {
                bubbleRenderer.renderBubble(nextBubble);
                bubbleState.activeBubbles.add(nextBubble);
            }

            if (bubbleState.waitingQueue.isEmpty() && bubbleState.activeBubbles.isEmpty()) {
                playerBubbleStates.remove(playerUuid);
            }
        } finally {
            bubbleState.lock.unlock();
        }
    }

    public void clear(FPlayer fPlayer) {
        PlayerBubbleState state = playerBubbleStates.remove(fPlayer.getUuid());
        if (state == null) return;

        clearBubbleState(state);
    }

    public void clear() {
        playerBubbleStates.forEach((uuid, state) -> clearBubbleState(state));
        playerBubbleStates.clear();
        bubbleRenderer.removeAllBubbles();
    }

    private void clearBubbleState(PlayerBubbleState state) {
        state.lock.lock();
        try {
            state.waitingQueue.clear();
            state.activeBubbles.forEach(bubble -> bubbleRenderer.removeBubbleIf(bubbleEntity -> bubbleEntity.getBubble().getId() == bubble.getId()));
            state.activeBubbles.clear();
        } finally {
            state.lock.unlock();
        }
    }

    private long calculateDuration(String message) {
        Message.Bubble config = fileFacade.message().bubble();

        int countWords = message.split(" ").length;
        return (long) (((countWords + config.handicapChars()) / config.readSpeed()) * 60) * 1000L;
    }

    private boolean isModern() {
        return packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19_4)
                && fileFacade.message().bubble().modern().enable();
    }

    private boolean isInteractionRiding() {
        return packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_3)
                && fileFacade.message().bubble().interaction().enable();
    }
}