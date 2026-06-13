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
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.service.TranslationCacheService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.SettingText;
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
    private final SocialService socialService;
    private final TranslationCacheService translationCacheService;

    public void startTicker() {
        taskScheduler.runPlayerAsyncTimer(fPlayer -> {
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


        String plainMessage = messagePipeline.buildPlain(MessageContext.builder()
                .sender(sender)
                .message(message)
                .flags(
                        new MessageFlag[]{MessageFlag.PLAYER_MESSAGE, MessageFlag.MENTION_MODULE, MessageFlag.INTERACTIVE_CHAT_COMPAT, MessageFlag.QUESTIONANSWER_MODULE, MessageFlag.ITEM_DETECTION, MessageFlag.OBJECT_SPRITE_PROCESSING, MessageFlag.OBJECT_PLAYER_HEAD_PROCESSING, MessageFlag.OBJECT_TEXTURE_PROCESSING, MessageFlag.REMOVE_DISABLED_TAGS, MessageFlag.VIOLATION_PROCESSING, MessageFlag.URL_PROCESSING},
                        new boolean[]{true, false, false, false, false, false, false, false, false, false, false}
                )
                .build()
        );

        List<Bubble> bubbles = splitMessageToBubbles(sender, plainMessage, receivers);

        state.waitingQueue.addAll(bubbles);

        warmTranslationCache(sender, plainMessage, receivers);
    }

    // Fire-and-forget cache warming: for every distinct online viewer locale that differs
    // from the sender's, kick off a translation of the WHOLE message so the renderer can
    // pick it up synchronously when the bubble surfaces. Gated on chat auto-translate.
    private void warmTranslationCache(@NonNull FPlayer sender, @NonNull String fullMessage, List<FPlayer> receivers) {
        if (!isBubbleTranslateEnabled()) return;
        if (receivers == null || receivers.isEmpty()) return;

        String senderLocale = socialService.getSetting(sender, SettingText.LOCALE);
        if (senderLocale == null) return;

        List<String> providers = fileFacade.message().format().translate().providers();
        if (providers == null || providers.isEmpty()) return;

        Set<String> targetLocales = new HashSet<>();
        for (FPlayer receiver : receivers) {
            if (receiver == null || receiver.isUnknown()) continue;
            String viewerLocale = socialService.getSetting(receiver, SettingText.LOCALE);
            if (viewerLocale == null || viewerLocale.equals(senderLocale)) continue;
            targetLocales.add(viewerLocale);
        }

        for (String targetLocale : targetLocales) {
            translationCacheService.translateAsync(senderLocale, targetLocale, fullMessage, providers);
        }
    }

    // Bubble auto-translate runs only when chat auto-translate is on AND the bubble
    // toggle is enabled (default true).
    private boolean isBubbleTranslateEnabled() {
        Message.Format.Translate translate = fileFacade.message().format().translate();
        boolean chatAuto = Boolean.TRUE.equals(translate.enable()) && !Boolean.FALSE.equals(translate.auto());
        boolean bubbleToggle = !Boolean.FALSE.equals(fileFacade.message().bubble().translate());
        return chatAuto && bubbleToggle;
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

        int maxLength = config.maxLength();
        int maxCount = config.maxCount();

        String senderLocale = socialService.getSetting(sender, SettingText.LOCALE);

        List<String> chunks = splitText(message, maxLength, maxCount, hintBufferLength, wordBreakHint);

        List<Bubble> bubbles = new ObjectArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            bubbles.add(buildBubble(
                    id, sender, chunks.get(i), message, senderLocale, i, chunks.size(),
                    duration, elevation, interactionHeight,
                    useInteractionRiding, useModernBubble, hasShadow, seeThrough, background,
                    animationTime, scale, billboard, receivers
            ));
        }

        return List.copyOf(bubbles);
    }

    // Symbol-level splitter shared by the original message and by re-splitting a
    // translation at render time so both produce identically-shaped chunks.
    public static List<String> splitText(String message, int maxLength, int maxCount, int hintBufferLength, String wordBreakHint) {
        if (message.length() <= maxLength) {
            return List.of(message);
        }

        List<String> chunks = new ObjectArrayList<>();
        int start = 0;

        while (start < message.length() && chunks.size() < maxCount) {
            int end = Math.min(start + maxLength, message.length());

            // the last
            if (end == message.length()) {
                chunks.add(message.substring(start).trim());
                break;
            }

            // looking for a separator back from the limit
            int breakAt = -1;
            for (int j = end; j > start && j > end - hintBufferLength; j--) {
                if (isNotLetter(message.charAt(j))) {
                    breakAt = j;
                    break;
                }
            }

            String chunk;
            int nextStart;
            if (breakAt != -1) {
                // split by separator, throw away separator itself
                chunk = message.substring(start, breakAt).trim();
                nextStart = breakAt + 1;
            } else {
                // word longer than maxLength, cut with hint
                chunk = message.substring(start, end) + wordBreakHint;
                nextStart = end;
            }

            chunks.add(chunk);
            start = nextStart;
        }

        return List.copyOf(chunks);
    }

    private Bubble buildBubble(int id, FPlayer sender, String message, String fullMessage, String senderLocale,
                               int chunkIndex, int chunkCount, long duration, float elevation, float interactionHeight,
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
                .fullMessage(fullMessage)
                .senderLocale(senderLocale)
                .chunkIndex(chunkIndex)
                .chunkCount(chunkCount)
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

    private static boolean isNotLetter(char symbol) {
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