package net.flectone.pulse.module.message.format.questionanswer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.*;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.format.questionanswer.listener.QuestionAnswerPulseListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.util.logging.FLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Singleton
public class QuestionAnswerModule extends AbstractModuleLocalization<Localization.Message.Format.QuestionAnswer> {

    private final Map<UUID, Boolean> processedQuestions = new WeakHashMap<>();
    private final Map<String, Sound> soundMap = new HashMap<>();
    @Getter private final Map<String, Cooldown> cooldownMap = new HashMap<>();
    @Getter private final Map<String, Pattern> patternMap = new HashMap<>();

    private final Message.Format.QuestionAnswer message;
    private final Permission.Message.Format.QuestionAnswer permission;
    private final FLogger fLogger;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public QuestionAnswerModule(FileResolver fileResolver,
                                FLogger fLogger,
                                ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getFormat().getQuestionAnswer());

        this.message = fileResolver.getMessage().getFormat().getQuestionAnswer();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getQuestionAnswer();
        this.fLogger = fLogger;
        this.listenerRegistry = listenerRegistry;
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

    public void sendAnswer(UUID processId, FEntity sender, FEntity receiver, String question) {
        if (processedQuestions.containsKey(processId)) return;

        processedQuestions.put(processId, true);

        sendAnswerLater(sender, receiver, question);
    }

    @Async(delay = 1L)
    public void sendAnswerLater(FEntity sender, FEntity receiver, String question) {
        Message.Format.QuestionAnswer.Question questionMessage = message.getQuestions().get(question);
        if (questionMessage == null) return;

        Range range = questionMessage.getRange();
        if (range.is(Range.Type.PLAYER) && !sender.equals(receiver)) return;
        if (!(receiver instanceof FPlayer fReceiver)) return;

        builder(sender)
                .receiver(fReceiver)
                .destination(questionMessage.getDestination())
                .format(questionAnswer -> questionAnswer.getQuestions().getOrDefault(question, ""))
                .sound(soundMap.get(question))
                .sendBuilt();
    }
}
