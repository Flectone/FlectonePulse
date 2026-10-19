package net.flectone.pulse.module.message.bubble;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.message.context.StringMessageContext;
import net.flectone.pulse.model.value.Destination;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.render.HeadTextRender;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.SocialService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.jspecify.annotations.NonNull;

import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BubbleModuleImpl implements BubbleModule {

    private final FileFacade fileFacade;
    private final TaskScheduler taskScheduler;
    private final ModuleController moduleController;
    private final HeadTextRender headTextRender;
    private final SocialService socialService;
    private final MessagePipeline messagePipeline;
    private final FLogger fLogger;

    private Predicate<String> disallowedPredicate;

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_BUBBLE;
    }

    @Override
    public Message.Bubble config() {
        return fileFacade.message().bubble();
    }

    @Override
    public Permission.Message.Bubble permission() {
        return fileFacade.permission().message().bubble();
    }

    @Override
    public Localization.Message.Bubble localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().bubble();
    }

    @Override
    public void onEnable() {
        if (config().disallowedInput().isEmpty()) {
            disallowedPredicate = null;
        } else {
            try {
                disallowedPredicate = Pattern.compile(config().disallowedInput()).asMatchPredicate();
            } catch (PatternSyntaxException e) {
                fLogger.warning(e);
            }
        }
    }

    @Override
    public void add(@NonNull FPlayer fPlayer, @NonNull String inputString, Set<FPlayer> receivers) {
        add(fPlayer, inputString, inputString, receivers);
    }

    @Override
    public void add(@NonNull FPlayer fPlayer, @NonNull String rawString, @NonNull String inputString, Set<FPlayer> receivers) {
        if (disallowedPredicate != null && disallowedPredicate.test(rawString)) return;
        if (config().destination().type() != Destination.Type.HEAD_TEXT) return;

        taskScheduler.runAsync(() -> {
            if (moduleController.isDisabledFor(this, fPlayer)) return;

            addMessage(fPlayer, inputString, receivers);
        });
    }

    private void addMessage(@NonNull FPlayer sender, @NonNull String message, Set<FPlayer> receivers) {
        if (!headTextRender.isCorrectHolder(sender.uuid())) return;

        message = messagePipeline.buildPlain(MessageContext.builder()
                .module(ModuleName.MESSAGE_BUBBLE)
                .sender(sender)
                .message(message)
                .flags(
                        new MessageFlag[]{MessageFlag.PLAYER_MESSAGE, MessageFlag.MENTION_MODULE, MessageFlag.INTERACTIVE_CHAT_COMPAT, MessageFlag.QUESTIONANSWER_MODULE, MessageFlag.ITEM_DETECTION, MessageFlag.OBJECT_SPRITE_PROCESSING, MessageFlag.OBJECT_PLAYER_HEAD_PROCESSING, MessageFlag.OBJECT_TEXTURE_PROCESSING, MessageFlag.REMOVE_DISABLED_TAGS, MessageFlag.VIOLATION_PROCESSING, MessageFlag.URL_PROCESSING},
                        new boolean[]{true, false, false, false, false, false, false, false, false, false, false}
                )
                .build()
        );

        String wordBreakHint = config().wordBreakHint();
        int hintBufferLength = config().hintBufferLength();
        int maxLength = config().maxLength();
        int maxCount = config().destination().headText().maxCount();

        if (message.length() <= maxLength) {
            show(sender, message, receivers);
            return;
        }

        int count = 0;
        int start = 0;

        while (start < message.length() && count < maxCount) {
            int end = Math.min(start + maxLength, message.length());

            // the last
            if (end == message.length()) {
                show(sender, message.substring(start).trim(), receivers);
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

            show(sender, chunk, receivers);

            count++;

            start = nextStart;
        }
    }

    private void show(@NonNull FPlayer sender, @NonNull String message, Set<FPlayer> receivers) {
        long lifeTime = calculateDuration(message);
        headTextRender.render(
                this.name(),
                sender,
                receivers,
                viewer -> createFormattedMessage(sender, viewer, message),
                config().destination().headText().withLifeTime(lifeTime)
        );
    }

    private Component createFormattedMessage(FPlayer sender, FPlayer receiver, String message) {
        MessageContext messageContext = MessageContext.builder()
                .module(this.name())
                .sender(sender)
                .receiver(receiver)
                .message(message)
                .flags(
                        new MessageFlag[]{MessageFlag.MENTION_MODULE, MessageFlag.INTERACTIVE_CHAT_COMPAT, MessageFlag.QUESTIONANSWER_MODULE, MessageFlag.PLAYER_MESSAGE},
                        new boolean[]{false, false, false, true}
                )
                .build();

        return messagePipeline.build(StringMessageContext.builder()
                .base(messageContext.toBuilder()
                        .module(this.name())
                        .message(localization(receiver).format())
                        .flag(MessageFlag.PLAYER_MESSAGE, false)
                        .tagResolver(messagePipeline.resolver("message", (_, _) -> Tag.inserting(messagePipeline.build(messageContext))))
                        .build()
                )
                .string(message)
                .build()
        );
    }

    private long calculateDuration(String message) {
        Message.Bubble config = fileFacade.message().bubble();

        int countWords = message.split(" ").length;
        return (long) (((countWords + config.handicapChars()) / config.readSpeed()) * 60) * 20;
    }

    private boolean isNotLetter(char symbol) {
        if (Character.isLetter(symbol)) return false;
        if (Character.isSpaceChar(symbol)) return true;

        return !Character.isDigit(symbol);
    }

}
