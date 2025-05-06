package net.flectone.pulse.module.message.format.questionanswer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.Cooldown;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Sound;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.util.Range;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static net.flectone.pulse.util.TagResolverUtil.emptyTagResolver;

@Singleton
public class QuestionAnswerModule extends AbstractModuleMessage<Localization.Message.Format.QuestionAnswer> implements MessageProcessor {

    private final WeakHashMap<UUID, Boolean> processedQuestions = new WeakHashMap<>();

    private final Map<String, Sound> soundMap = new HashMap<>();
    private final Map<String, Cooldown> cooldownMap = new HashMap<>();
    private final Map<String, Pattern> patternMap = new HashMap<>();

    private final Message.Format.QuestionAnswer message;
    private final Permission.Message.Format.QuestionAnswer permission;

    private final PermissionChecker permissionChecker;
    private final FLogger fLogger;

    @Inject
    public QuestionAnswerModule(FileManager fileManager,
                                PermissionChecker permissionChecker,
                                FLogger fLogger,
                                MessageProcessRegistry messageProcessRegistry) {
        super(localization -> localization.getMessage().getFormat().getQuestionAnswer());

        this.permissionChecker = permissionChecker;
        this.fLogger = fLogger;

        message = fileManager.getMessage().getFormat().getQuestionAnswer();
        permission = fileManager.getPermission().getMessage().getFormat().getQuestionAnswer();

        messageProcessRegistry.register(100, this);
    }

    @Override
    public void reload() {
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
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        if (!messageContext.isQuestion()) return;
        if (!messageContext.isUserMessage()) return;

        String message = replace(messageContext.getSender(), messageContext.getMessage());

        messageContext.setMessage(message);
        messageContext.addTagResolvers(questionAnswerTag(messageContext.getProcessId(), messageContext.getSender(), messageContext.getReceiver()));
    }

    private String replace(FEntity sender, String message) {
        if (checkModulePredicates(sender)) return message;

        for (Map.Entry<String, Pattern> entry : patternMap.entrySet()) {
            Permission.Message.Format.QuestionAnswer.Question questionPermission = permission.getQuestions().get(entry.getKey());
            if (questionPermission != null && !permissionChecker.check(sender, questionPermission.getAsk())) continue;

            Matcher matcher = entry.getValue().matcher(message);
            if (!matcher.find()) continue;

            Cooldown cooldown = cooldownMap.get(entry.getKey());
            if (cooldown != null && cooldown.isCooldown(sender.getUuid())) continue;

            message += "<question:'" + entry.getKey() + "'>";
        }

        return message;
    }

    private TagResolver questionAnswerTag(UUID processId, FEntity sender, FEntity receiver) {
        String tag = "question";
        if (checkModulePredicates(sender)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
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
    public void sendAnswerLater(FEntity sender, FEntity receiver, String question) {
        Message.Format.QuestionAnswer.Question questionMessage = message.getQuestions().get(question);
        if (questionMessage == null) return;

        int range = questionMessage.getRange();
        if (range == Range.PLAYER && !sender.equals(receiver)) return;
        if (!(receiver instanceof FPlayer fReceiver)) return;

        builder(sender)
                .receiver(fReceiver)
                .destination(questionMessage.getDestination())
                .format(questionAnswer -> questionAnswer.getQuestions().getOrDefault(question, ""))
                .sound(soundMap.get(question))
                .sendBuilt();
    }
}
