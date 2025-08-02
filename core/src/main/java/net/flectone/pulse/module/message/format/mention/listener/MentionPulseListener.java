package net.flectone.pulse.module.message.format.mention.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.mention.MentionModule;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.util.Optional;
import java.util.UUID;

@Singleton
public class MentionPulseListener implements PulseListener {

    private final Message.Format.Mention message;
    private final Permission.Message.Format.Mention permission;
    private final MentionModule mentionModule;
    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final IntegrationModule integrationModule;
    private final MessagePipeline messagePipeline;

    @Inject
    public MentionPulseListener(FileResolver fileResolver,
                                MentionModule mentionModule,
                                FPlayerService fPlayerService,
                                PermissionChecker permissionChecker,
                                IntegrationModule integrationModule,
                                MessagePipeline messagePipeline) {
        this.message = fileResolver.getMessage().getFormat().getMention();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getMention();
        this.mentionModule = mentionModule;
        this.fPlayerService = fPlayerService;
        this.permissionChecker = permissionChecker;
        this.integrationModule = integrationModule;
        this.messagePipeline = messagePipeline;
    }

    @Pulse
    public void onMessageProcessingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.MENTION)) return;

        String processedMessage = replace(messageContext.getSender(), messageContext.getMessage());
        messageContext.setMessage(processedMessage);

        FEntity sender = messageContext.getSender();
        if (mentionModule.checkModulePredicates(sender)) return;

        UUID processId = messageContext.getProcessId();
        FPlayer receiver = messageContext.getReceiver();
        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.MENTION, (argumentQueue, context) -> {
            Tag.Argument mentionTag = argumentQueue.peek();
            if (mentionTag == null) return Tag.selfClosingInserting(Component.empty());

            String mention = mentionTag.value();
            if (mention.isEmpty()) {
                return Tag.preProcessParsed(message.getTrigger() + mention);
            }

            Optional<String> group = integrationModule.getGroups().stream()
                    .filter(name -> name.equalsIgnoreCase(mention))
                    .findFirst();

            if (group.isPresent()) {
                if (receiver instanceof FPlayer mentionFPlayer
                        && !permissionChecker.check(mentionFPlayer, permission.getBypass())
                        && permissionChecker.check(mentionFPlayer, "group." + group.get())) {
                    mentionModule.sendMention(processId, mentionFPlayer);
                }
            } else {
                FPlayer mentionFPlayer = fPlayerService.getFPlayer(mention);
                if (mentionFPlayer.equals(receiver) && !permissionChecker.check(mentionFPlayer, permission.getBypass())) {
                    mentionModule.sendMention(processId, mentionFPlayer);
                }
            }

            String format = mentionModule.resolveLocalization(receiver).getFormat()
                    .replace("<player>", mention)
                    .replace("<target>", mention);

            return Tag.selfClosingInserting(messagePipeline.builder(receiver, format).build());
        });
    }

    private String replace(FEntity sender, String message) {
        String[] words = message.split(" ");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            if (!word.startsWith(this.message.getTrigger())) continue;

            String wordWithoutPrefix = word.replaceFirst(this.message.getTrigger(), "");

            boolean isMention = !fPlayerService.getFPlayer(wordWithoutPrefix).isUnknown()
                    || integrationModule.getGroups().contains(wordWithoutPrefix)
                    && permissionChecker.check(sender, permission.getGroup());

            if (!isMention) continue;

            words[i] = "<mention:" + wordWithoutPrefix + ">";
            break;
        }

        return String.join(" ", words);
    }
}
