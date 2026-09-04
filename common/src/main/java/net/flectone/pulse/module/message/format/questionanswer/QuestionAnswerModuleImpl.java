package net.flectone.pulse.module.message.format.questionanswer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.checker.CooldownChecker;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Pair;
import net.flectone.pulse.model.value.Range;
import net.flectone.pulse.model.value.Sound;
import net.flectone.pulse.module.message.format.questionanswer.listener.PulseQuestionAnswerListener;
import net.flectone.pulse.module.message.format.questionanswer.model.QuestionAnswerMessageContext;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.SocialService;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class QuestionAnswerModuleImpl implements QuestionAnswerModule {

    private final Map<String, Pattern> patternMap = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final FLogger fLogger;
    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;
    private final CooldownChecker cooldownChecker;
    private final TaskScheduler taskScheduler;
    private final MessageDispatcher messageDispatcher;
    private final MessagePipeline messagePipeline;
    private final ModuleController moduleController;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        config().questions().forEach((key, questionMessage) -> {
            try {
                patternMap.put(key, Pattern.compile(questionMessage.target()));
            } catch (PatternSyntaxException e) {
                fLogger.warning(e);
            }
        });

        listenerRegistry.register(PulseQuestionAnswerListener.class);
    }

    @Override
    public Set<PermissionSetting> permissions() {
        Set<PermissionSetting> permissions = new LinkedHashSet<>(QuestionAnswerModule.super.permissions());
        permissions.addAll(permission().questions().values().stream().flatMap(question ->
                        Stream.of(
                                question,
                                question.sound(),
                                question.cooldownBypass()
                        )).toList());
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public void onDisable() {
        patternMap.clear();
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_QUESTIONANSWER;
    }

    @Override
    public Message.Format.QuestionAnswer config() {
        return fileFacade.message().format().questionAnswer();
    }

    @Override
    public Permission.Message.Format.QuestionAnswer permission() {
        return fileFacade.permission().message().format().questionAnswer();
    }

    @Override
    public Localization.Message.Format.QuestionAnswer localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().format().questionAnswer();
    }

    @Override
    public MessageContext format(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;

        String contextMessage = messageContext.message();
        StringBuilder result = new StringBuilder(contextMessage);

        for (Map.Entry<String, Pattern> entry : patternMap.entrySet()) {
            Permission.Message.Format.QuestionAnswer.Question questionPermission = permission().questions().get(entry.getKey());
            if (questionPermission != null && !permissionChecker.check(sender, questionPermission)) continue;

            Matcher matcher = entry.getValue().matcher(contextMessage);
            if (!matcher.find()) continue;

            Message.Format.QuestionAnswer.Question question = config().questions().get(entry.getKey());
            if (question != null
                    && (questionPermission != null && !permissionChecker.check(sender, questionPermission.cooldownBypass()))
                    && cooldownChecker.check(sender.uuid(), question.cooldown(), this.getClass().getName() + entry.getKey())) continue;

            result.append("<question:'").append(entry.getKey()).append("'>");
        }

        return messageContext.withMessage(result.toString());
    }

    @Override
    public MessageContext addTag(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;

        FEntity receiver = messageContext.receiver();

        return messageContext.addTagResolver(messagePipeline.resolver(MessagePipeline.ReplacementTag.QUESTION.getTagName(), (argumentQueue, _) -> {
            Tag.Argument questionTag = argumentQueue.peek();
            if (questionTag == null) return MessagePipeline.ReplacementTag.emptyTag();

            String questionKey = questionTag.value();
            if (questionKey.isEmpty()) return MessagePipeline.ReplacementTag.emptyTag();

            sendAnswer(sender, receiver, questionKey);

            return MessagePipeline.ReplacementTag.emptyTag();
        }));
    }

    private void sendAnswer(FEntity sender, FEntity receiver, String question) {
        Message.Format.QuestionAnswer.Question questionMessage = config().questions().get(question);
        if (questionMessage == null) return;

        Range range = questionMessage.range();
        if (range.is(Range.Type.PLAYER) && !sender.equals(receiver)) return;
        if (!(receiver instanceof FPlayer fReceiver)) return;

        Permission.Message.Format.QuestionAnswer.Question questionPermission = permission().questions().get(question);
        Pair<Sound, PermissionSetting> sound = Pair.of(questionMessage.sound(), questionPermission == null ? null : questionPermission.sound());

        taskScheduler.runAsyncLater(() -> messageDispatcher.dispatch(this, EventMetadata.builder()
                .filter(fReceiver)
                .destination(questionMessage.destination())
                .sound(sound)
                .messageContext(fResolver -> QuestionAnswerMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(sender)
                                .receiver(fResolver)
                                .message(localization(fReceiver).questions().getOrDefault(question, ""))
                                .build()
                        )
                        .string(question)
                        .question(question)
                        .build()
                )
                .build()
        ), 1L);
    }
}
