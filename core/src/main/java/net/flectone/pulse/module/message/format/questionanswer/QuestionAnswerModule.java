package net.flectone.pulse.module.message.format.questionanswer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Cooldown;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.format.questionanswer.listener.QuestionAnswerPulseListener;
import net.flectone.pulse.module.message.format.questionanswer.model.QuestionAnswerMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Singleton
public class QuestionAnswerModule extends AbstractModuleLocalization<Localization.Message.Format.QuestionAnswer> {

    private final Map<UUID, Boolean> processedQuestions = new WeakHashMap<>();
    private final Map<String, Sound> soundMap = new HashMap<>();
    private final Map<String, Cooldown> cooldownMap = new HashMap<>();
    private final Map<String, Pattern> patternMap = new HashMap<>();

    private final Message.Format.QuestionAnswer message;
    private final Permission.Message.Format.QuestionAnswer permission;
    private final FLogger fLogger;
    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;

    @Inject
    public QuestionAnswerModule(FileResolver fileResolver,
                                FLogger fLogger,
                                ListenerRegistry listenerRegistry,
                                PermissionChecker permissionChecker) {
        super(localization -> localization.getMessage().getFormat().getQuestionAnswer(), MessageType.QUESTION_ANSWER);

        this.message = fileResolver.getMessage().getFormat().getQuestionAnswer();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getQuestionAnswer();
        this.fLogger = fLogger;
        this.listenerRegistry = listenerRegistry;
        this.permissionChecker = permissionChecker;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        message.getQuestions().forEach((key, questionMessage) -> {

            try {
                patternMap.put(key, Pattern.compile(questionMessage.getTarget()));
            } catch (PatternSyntaxException e) {
                fLogger.warning(e);
            }

            Permission.Message.Format.QuestionAnswer.Question questionPermission = permission.getQuestions().get(key);
            if (questionPermission == null) return;

            registerPermission(questionPermission.getAsk());
            soundMap.put(key, createSound(questionMessage.getSound(), questionPermission.getSound()));
            cooldownMap.put(key, createCooldown(questionMessage.getCooldown(), questionPermission.getCooldownBypass()));
        });

        listenerRegistry.register(QuestionAnswerPulseListener.class);
    }

    @Override
    public void onDisable() {
        processedQuestions.clear();
        soundMap.clear();
        cooldownMap.clear();
        patternMap.clear();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public void format(MessageContext messageContext) {
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;

        String contextMessage = messageContext.getMessage();
        StringBuilder result = new StringBuilder(contextMessage);

        for (Map.Entry<String, Pattern> entry : patternMap.entrySet()) {
            Permission.Message.Format.QuestionAnswer.Question questionPermission = permission.getQuestions().get(entry.getKey());
            if (questionPermission != null && !permissionChecker.check(sender, questionPermission.getAsk())) continue;

            Matcher matcher = entry.getValue().matcher(contextMessage);
            if (!matcher.find()) continue;

            Cooldown cooldown = cooldownMap.get(entry.getKey());
            if (cooldown != null && cooldown.isCooldown(sender.getUuid())) continue;

            result.append("<question:'").append(entry.getKey()).append("'>");
        }

        messageContext.setMessage(result.toString());
    }

    public void addTag(MessageContext messageContext) {
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;

        UUID processId = messageContext.getMessageUUID();
        FEntity receiver = messageContext.getReceiver();

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.QUESTION, (argumentQueue, context) -> {
            Tag.Argument questionTag = argumentQueue.peek();
            if (questionTag == null) return Tag.selfClosingInserting(Component.empty());

            String questionKey = questionTag.value();
            if (questionKey.isEmpty()) return Tag.selfClosingInserting(Component.empty());

            sendAnswer(processId, sender, receiver, questionKey);

            return Tag.selfClosingInserting(Component.empty());
        });
    }

    private void sendAnswer(UUID processId, FEntity sender, FEntity receiver, String question) {
        if (processedQuestions.containsKey(processId)) return;

        processedQuestions.put(processId, true);

        sendAnswerLater(sender, receiver, question);
    }

    @Async(delay = 1L)
    private void sendAnswerLater(FEntity sender, FEntity receiver, String question) {
        Message.Format.QuestionAnswer.Question questionMessage = message.getQuestions().get(question);
        if (questionMessage == null) return;

        Range range = questionMessage.getRange();
        if (range.is(Range.Type.PLAYER) && !sender.equals(receiver)) return;
        if (!(receiver instanceof FPlayer fReceiver)) return;

        sendMessage(QuestionAnswerMetadata.<Localization.Message.Format.QuestionAnswer>builder()
                .sender(sender)
                .filterPlayer(fReceiver)
                .format(questionAnswer -> questionAnswer.getQuestions().getOrDefault(question, ""))
                .question(question)
                .destination(questionMessage.getDestination())
                .sound(soundMap.get(question))
                .build()
        );
    }
}
