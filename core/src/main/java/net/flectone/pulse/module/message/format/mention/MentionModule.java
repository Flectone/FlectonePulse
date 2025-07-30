package net.flectone.pulse.module.message.format.mention;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

@Singleton
public class MentionModule extends AbstractModuleMessage<Localization.Message.Format.Mention> implements MessageProcessor {

    private final WeakHashMap<UUID, Boolean> processedMentions = new WeakHashMap<>();

    private final Message.Format.Mention message;
    private final Permission.Message.Format.Mention permission;
    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final IntegrationModule integrationModule;
    private final MessagePipeline messagePipeline;
    private final MessageProcessRegistry messageProcessRegistry;

    @Inject
    public MentionModule(FileResolver fileResolver,
                         FPlayerService fPlayerService,
                         PermissionChecker permissionChecker,
                         IntegrationModule integrationModule,
                         MessagePipeline messagePipeline,
                         MessageProcessRegistry messageProcessRegistry) {
        super(localization -> localization.getMessage().getFormat().getMention());

        this.message = fileResolver.getMessage().getFormat().getMention();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getMention();
        this.fPlayerService = fPlayerService;
        this.permissionChecker = permissionChecker;
        this.integrationModule = integrationModule;
        this.messagePipeline = messagePipeline;
        this.messageProcessRegistry = messageProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        registerPermission(permission.getGroup());
        registerPermission(permission.getBypass());

        messageProcessRegistry.register(100, this);
    }

    @Override
    public void onDisable() {
        processedMentions.clear();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        if (!messageContext.isMention()) return;

        String processedMessage = replace(messageContext.getSender(), messageContext.getMessage());
        messageContext.setMessage(processedMessage);

        FEntity sender = messageContext.getSender();
        if (checkModulePredicates(sender)) return;

        UUID processId = messageContext.getProcessId();
        FEntity receiver = messageContext.getReceiver();
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
                    sendMention(processId, mentionFPlayer);
                }
            } else {
                FPlayer mentionFPlayer = fPlayerService.getFPlayer(mention);
                if (mentionFPlayer.equals(receiver) && !permissionChecker.check(mentionFPlayer, permission.getBypass())) {
                    sendMention(processId, mentionFPlayer);
                }
            }

            String format = resolveLocalization(receiver).getFormat()
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

    private void sendMention(UUID processId, FPlayer fPlayer) {
        if (processedMentions.containsKey(processId)) return;

        processedMentions.put(processId, true);

        playSound(fPlayer);

        builder(fPlayer)
                .destination(message.getDestination())
                .format(Localization.Message.Format.Mention::getPerson)
                .sound(null)
                .sendBuilt();
    }
}
