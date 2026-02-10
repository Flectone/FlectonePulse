package net.flectone.pulse.module.message.format.questionanswer;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.format.questionanswer.listener.QuestionAnswerPulseListener;
import net.flectone.pulse.module.message.format.questionanswer.model.QuestionAnswerMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.checker.CooldownChecker;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.incendo.cloud.type.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class QuestionAnswerModule extends AbstractModuleLocalization<Localization.Message.Format.QuestionAnswer> {

    private final Map<UUID, Boolean> processedQuestions = new WeakHashMap<>();
    private final Map<String, Pattern> patternMap = new HashMap<>();

    private final FileFacade fileFacade;
    private final FLogger fLogger;
    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;
    private final CooldownChecker cooldownChecker;
    private final TaskScheduler taskScheduler;

    @Override
    public void onEnable() {
        super.onEnable();

        config().questions().forEach((key, questionMessage) -> {
            try {
                patternMap.put(key, Pattern.compile(questionMessage.target()));
            } catch (PatternSyntaxException e) {
                fLogger.warning(e);
            }
        });

        listenerRegistry.register(QuestionAnswerPulseListener.class);
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder()
                .addAll(permission().questions().values().stream().flatMap(question ->
                        Stream.of(
                                question,
                                question.sound(),
                                question.cooldownBypass()
                        )).toList()
                );
    }

    @Override
    public void onDisable() {
        super.onDisable();

        processedQuestions.clear();
        patternMap.clear();
    }

    @Override
    public MessageType messageType() {
        return MessageType.QUESTION_ANSWER;
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
    public Localization.Message.Format.QuestionAnswer localization(FEntity sender) {
        return fileFacade.localization(sender).message().format().questionAnswer();
    }

    public MessageContext format(MessageContext messageContext) {
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return messageContext;

        FEntity sender = messageContext.sender();
        if (isModuleDisabledFor(sender)) return messageContext;

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
                    && cooldownChecker.check(sender.getUuid(), question.cooldown(), this.getClass().getName() + entry.getKey())) continue;

            result.append("<question:'").append(entry.getKey()).append("'>");
        }

        return messageContext.withMessage(result.toString());
    }

    public MessageContext addTag(MessageContext messageContext) {
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return messageContext;

        FEntity sender = messageContext.sender();
        if (isModuleDisabledFor(sender)) return messageContext;

        UUID processId = messageContext.messageUUID();
        FEntity receiver = messageContext.receiver();

        return messageContext.addTagResolver(MessagePipeline.ReplacementTag.QUESTION, (argumentQueue, context) -> {
            Tag.Argument questionTag = argumentQueue.peek();
            if (questionTag == null) return MessagePipeline.ReplacementTag.emptyTag();

            String questionKey = questionTag.value();
            if (questionKey.isEmpty()) return MessagePipeline.ReplacementTag.emptyTag();

            sendAnswer(processId, sender, receiver, questionKey);

            return MessagePipeline.ReplacementTag.emptyTag();
        });
    }

    private void sendAnswer(UUID processId, FEntity sender, FEntity receiver, String question) {
        if (processedQuestions.containsKey(processId)) return;

        processedQuestions.put(processId, true);

        sendAnswerLater(sender, receiver, question);
    }

    private void sendAnswerLater(FEntity sender, FEntity receiver, String question) {
        Message.Format.QuestionAnswer.Question questionMessage = config().questions().get(question);
        if (questionMessage == null) return;

        Range range = questionMessage.range();
        if (range.is(Range.Type.PLAYER) && !sender.equals(receiver)) return;
        if (!(receiver instanceof FPlayer fReceiver)) return;

        Permission.Message.Format.QuestionAnswer.Question questionPermission = permission().questions().get(question);
        Pair<Sound, PermissionSetting> sound = Pair.of(questionMessage.sound(), questionPermission == null ? null : questionPermission.sound());

        taskScheduler.runAsyncLater(() -> sendMessage(QuestionAnswerMetadata.<Localization.Message.Format.QuestionAnswer>builder()
                .base(EventMetadata.<Localization.Message.Format.QuestionAnswer>builder()
                        .sender(sender)
                        .filterPlayer(fReceiver)
                        .format(questionAnswer -> questionAnswer.questions().getOrDefault(question, ""))
                        .destination(questionMessage.destination())
                        .sound(sound)
                        .build()
                )
                .question(question)
                .build()
        ), 1L);
    }
}
