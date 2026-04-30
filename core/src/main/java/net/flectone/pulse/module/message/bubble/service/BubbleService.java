package net.flectone.pulse.module.message.bubble.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.bubble.model.Bubble;
import net.flectone.pulse.module.message.bubble.model.ModernBubble;
import net.flectone.pulse.module.message.bubble.render.BubbleRender;
import net.flectone.pulse.processing.converter.ColorConverter;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.generator.RandomGenerator;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BubbleService {

    private static final long MIN_DISPLAY_TIME = 2000L;

    private final Map<UUID, PlayerBubbleState> playerBubbleStates = new ConcurrentHashMap<>();
    private final FileFacade fileFacade;
    private final BubbleRender bubbleRender;
    private final ColorConverter colorConverter;
    private final TaskScheduler taskScheduler;
    private final RandomGenerator randomUtil;
    private final MessagePipeline messagePipeline;

    public void startTicker() {
        taskScheduler.runPlayerRegionTimer(fPlayer -> {
            PlayerBubbleState state = playerBubbleStates.get(fPlayer.uuid());
            if (state == null) return;

            processBubbleQueue(fPlayer.uuid(), state);
        }, 1L);
    }

    public void addMessage(@NonNull FPlayer sender, @NonNull String message, List<FPlayer> receivers) {
        if (!bubbleRender.isCorrectPlayer(sender)) return;

        PlayerBubbleState state = playerBubbleStates.computeIfAbsent(
                sender.uuid(),
                _ -> new PlayerBubbleState(new ConcurrentLinkedQueue<>(), new ConcurrentLinkedQueue<>(), new ReentrantLock())
        );

        MessageContext messageContext = messagePipeline.createContext(sender, message)
                .addFlags(
                        new MessageFlag[]{MessageFlag.PLAYER_MESSAGE, MessageFlag.MENTION_MODULE, MessageFlag.INTERACTIVE_CHAT_COMPAT, MessageFlag.QUESTIONANSWER_MODULE, MessageFlag.ITEM_DETECTION, MessageFlag.OBJECT_SPRITE_PROCESSING, MessageFlag.OBJECT_PLAYER_HEAD_PROCESSING, MessageFlag.OBJECT_TEXTURE_PROCESSING, MessageFlag.REMOVE_DISABLED_TAGS, MessageFlag.URL_PROCESSING},
                        new boolean[]{true, false, false, false, false, false, false, false, false, false}
                );

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

        boolean useModernBubble = bubbleRender.isModern();
        boolean useInteractionRiding = bubbleRender.isInteractionRiding();

        int hintBufferLength = config.hintBufferLength();
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
        if (message.length() <= maxLength) return List.of(buildBubble(
                id, sender, message, duration, elevation, interactionHeight,
                useInteractionRiding, useModernBubble, hasShadow, seeThrough, background,
                animationTime, scale, billboard, receivers
        ));

        List<Bubble> bubbles = new ObjectArrayList<>();

        StringBuilder line = new StringBuilder();

        char[] symbols = message.toCharArray();
        for (int i = 0; i < symbols.length; i++) {
            char symbol = symbols[i];

            boolean isNotLetter = isNotLetter(symbol);
            int leftSymbolsLimitCount = maxLength - line.length();
            if (line.length() < maxLength && (!isNotLetter || leftSymbolsLimitCount > hintBufferLength)) {
                // going to the limit
                line.append(symbol);
                continue;
            }

            String bubbleMessage;
            if (isNotLetter) {
                // if it's not a letter, we can break line without hint
                bubbleMessage = line.toString() + symbol;
            } else {
                // we need to add all the symbols up to the limit
                for (int k = 0; k <= leftSymbolsLimitCount && i < symbols.length; i++, k++) {
                    line.append(symbols[i]);
                }

                // update last symbol
                symbol = symbols[i];

                // symbol may not be a letter, so hint is not needed
                bubbleMessage = line.toString() + (isNotLetter(symbol) ? symbol : wordBreakHint);

                // need to step back, because 'k' moved 'i' further when exiting 'for'
                i--;
            }

            line.setLength(0);

            bubbles.add(buildBubble(
                    id, sender, bubbleMessage.trim(), duration, elevation, interactionHeight,
                    useInteractionRiding, useModernBubble, hasShadow, seeThrough, background,
                    animationTime, scale, billboard, receivers
            ));

            if (bubbles.size() == config.maxCount()) {
                return List.copyOf(bubbles);
            }
        }

        if (!line.isEmpty()) {
            bubbles.add(buildBubble(
                    id, sender, line.toString().trim(), duration, elevation, interactionHeight,
                    useInteractionRiding, useModernBubble, hasShadow, seeThrough, background,
                    animationTime, scale, billboard, receivers
            ));
        }

        return List.copyOf(bubbles);
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
                bubbleRender.removeBubbleIf(filterBubble -> filterBubble.equals(bubble));
                return true;
            });

            int maxCount = fileFacade.message().bubble().maxCount();
            if (bubbleState.activeBubbles.size() >= maxCount) {
                bubbleState.activeBubbles.stream()
                        .min(Comparator.comparingLong(Bubble::getExpireTime))
                        .map(Bubble::getExpireTime)
                        .ifPresent(lastActiveExpireTime -> bubbleState.waitingQueue.removeIf(bubble ->
                                bubble.getExpireTime() - lastActiveExpireTime < MIN_DISPLAY_TIME)
                        );

                return;
            }

            Bubble nextBubble = bubbleState.waitingQueue.poll();
            if (nextBubble != null && !nextBubble.isCreated()) {
                bubbleRender.renderBubble(nextBubble);
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
        PlayerBubbleState state = playerBubbleStates.remove(fPlayer.uuid());
        if (state == null) return;

        clearBubbleState(state);
    }

    public void clear() {
        playerBubbleStates.forEach((_, state) -> clearBubbleState(state));
        playerBubbleStates.clear();
        bubbleRender.removeAllBubbles();
    }

    private void clearBubbleState(PlayerBubbleState state) {
        state.lock.lock();
        try {
            state.waitingQueue.clear();
            state.activeBubbles.forEach(bubble -> bubbleRender.removeBubbleIf(filterBubble -> filterBubble.equals(bubble)));
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

    private boolean isNotLetter(char symbol) {
        if (Character.isLetter(symbol)) return false;
        if (Character.isSpaceChar(symbol)) return true;

        return !Character.isDigit(symbol);
    }

    private record PlayerBubbleState(
            Queue<Bubble> waitingQueue,
            Queue<Bubble> activeBubbles,
            ReentrantLock lock
    ) {
    }

}