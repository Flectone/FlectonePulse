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
import net.flectone.pulse.model.event.message.context.ComponentMessageContext;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Destination;
import net.flectone.pulse.model.value.HeadText;
import net.flectone.pulse.module.message.bubble.splitter.BubbleMessageSplitter;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.render.HeadTextRender;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.serializer.ComponentSerializer;
import net.flectone.pulse.service.NearbyService;
import net.flectone.pulse.service.SocialService;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;
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
    private final NearbyService nearbyService;
    private final MessagePipeline messagePipeline;
    private final ComponentSerializer componentSerializer;
    private final BubbleMessageSplitter bubbleMessageSplitter;
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

        Set<UUID> nearby = nearbyService.getNearbyPlayers(sender.uuid(), config().destination().headText().distance());
        if (nearby.isEmpty()) return;

        for (FPlayer receiver : receivers) {
            if (receiver.isUnknown() || receiver.isConsole()) continue;
            if (!nearby.contains(receiver.uuid())) continue;

            show(sender, receiver, message);
        }
    }

    private void show(@NonNull FPlayer sender, @NonNull FPlayer receiver, @NonNull String message) {
        Message.Bubble config = config();
        HeadText headText = config.destination().headText();

        Component component = messagePipeline.build(MessageContext.builder()
                .module(this.name())
                .sender(sender)
                .receiver(receiver)
                .message(message)
                .flags(
                        new MessageFlag[]{MessageFlag.MENTION_MODULE, MessageFlag.INTERACTIVE_CHAT_COMPAT, MessageFlag.QUESTIONANSWER_MODULE, MessageFlag.PLAYER_MESSAGE, MessageFlag.VIOLATION_PROCESSING},
                        new boolean[]{false, false, false, true, false}
                )
                .build()
        );

        List<Component> chunks = bubbleMessageSplitter.split(
                component,
                config.maxLength(),
                config.hintBufferLength(),
                config.wordBreakHint(),
                headText.maxCount()
        );

        for (Component chunk : chunks) {
            long lifeTime = calculateDuration(config, componentSerializer.toPlain(chunk));

            headTextRender.render(
                    this.name(),
                    sender,
                    receiver,
                    _ -> messagePipeline.build(ComponentMessageContext.builder()
                            .base(MessageContext.builder()
                                    .module(this.name())
                                    .sender(sender)
                                    .receiver(receiver)
                                    .message(localization(receiver).format())
                                    .flags(
                                            new MessageFlag[]{MessageFlag.MENTION_MODULE, MessageFlag.INTERACTIVE_CHAT_COMPAT, MessageFlag.QUESTIONANSWER_MODULE, MessageFlag.PLAYER_MESSAGE, MessageFlag.VIOLATION_PROCESSING},
                                            new boolean[]{false, false, false, false, false}
                                    )
                                    .tagResolver(messagePipeline.messageTag(chunk))
                                    .build()
                            )
                            .component(chunk)
                            .build()
                    ),
                    headText.withLifeTime(lifeTime)
            );
        }
    }

    private long calculateDuration(Message.Bubble config, String message) {
        int countWords = message.split(" ").length;
        return (long) (((countWords + config.handicapChars()) / config.readSpeed()) * 60) * 20;
    }

}
