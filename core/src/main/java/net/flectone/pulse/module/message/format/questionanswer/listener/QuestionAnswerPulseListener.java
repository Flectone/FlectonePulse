package net.flectone.pulse.module.message.format.questionanswer.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.Cooldown;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.questionanswer.QuestionAnswerModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.resolver.FileResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class QuestionAnswerPulseListener implements PulseListener {

    private final Permission.Message.Format.QuestionAnswer permission;
    private final QuestionAnswerModule questionAnswerModule;
    private final PermissionChecker permissionChecker;

    @Inject
    public QuestionAnswerPulseListener(FileResolver fileResolver,
                                       QuestionAnswerModule questionAnswerModule,
                                       PermissionChecker permissionChecker) {
        this.permission = fileResolver.getPermission().getMessage().getFormat().getQuestionAnswer();
        this.questionAnswerModule = questionAnswerModule;
        this.permissionChecker = permissionChecker;
    }

    @Pulse
    public void onMessageProcessingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.QUESTION)) return;
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        FEntity sender = messageContext.getSender();
        if (questionAnswerModule.checkModulePredicates(sender)) return;

        String processedMessage = replace(messageContext.getSender(), messageContext.getMessage());
        messageContext.setMessage(processedMessage);

        UUID processId = messageContext.getProcessId();
        FEntity receiver = messageContext.getReceiver();

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.QUESTION, (argumentQueue, context) -> {
            Tag.Argument questionTag = argumentQueue.peek();
            if (questionTag == null) return Tag.selfClosingInserting(Component.empty());

            String questionKey = questionTag.value();
            if (questionKey.isEmpty()) return Tag.selfClosingInserting(Component.empty());

            questionAnswerModule.sendAnswer(processId, sender, receiver, questionKey);

            return Tag.selfClosingInserting(Component.empty());
        });
    }

    private String replace(FEntity sender, String message) {
        StringBuilder result = new StringBuilder(message);

        for (Map.Entry<String, Pattern> entry : questionAnswerModule.getPatternMap().entrySet()) {
            Permission.Message.Format.QuestionAnswer.Question questionPermission = permission.getQuestions().get(entry.getKey());
            if (questionPermission != null && !permissionChecker.check(sender, questionPermission.getAsk())) continue;

            Matcher matcher = entry.getValue().matcher(message);
            if (!matcher.find()) continue;

            Cooldown cooldown = questionAnswerModule.getCooldownMap().get(entry.getKey());
            if (cooldown != null && cooldown.isCooldown(sender.getUuid())) continue;

            result.append("<question:'").append(entry.getKey()).append("'>");
        }

        return result.toString();
    }

}
