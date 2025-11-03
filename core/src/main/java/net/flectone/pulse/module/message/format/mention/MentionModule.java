package net.flectone.pulse.module.message.format.mention;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.mention.listener.MentionPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MentionModule extends AbstractModuleLocalization<Localization.Message.Format.Mention> {

    private final WeakHashMap<UUID, Boolean> processedMentions = new WeakHashMap<>();

    private final @Named("mentionMessage") Cache<String, String> messageCache;
    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;
    private final FPlayerService fPlayerService;
    private final IntegrationModule integrationModule;
    private final PermissionChecker permissionChecker;
    private final MessagePipeline messagePipeline;
    private final FLogger fLogger;

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().getSound(), permission().getSound());

        registerPermission(permission().getGroup());
        registerPermission(permission().getBypass());

        listenerRegistry.register(MentionPulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        processedMentions.clear();
        messageCache.invalidateAll();
    }

    @Override
    public MessageType messageType() {
        return MessageType.MENTION;
    }

    @Override
    public Message.Format.Mention config() {
        return fileResolver.getMessage().getFormat().getMention();
    }

    @Override
    public Permission.Message.Format.Mention permission() {
        return fileResolver.getPermission().getMessage().getFormat().getMention();
    }

    @Override
    public Localization.Message.Format.Mention localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getFormat().getMention();
    }

    public void format(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;

        String contextMessage = messageContext.getMessage();
        if (StringUtils.isEmpty(contextMessage)) return;

        String formattedMessage;
        try {
            formattedMessage = messageCache.get(contextMessage, () -> replace(contextMessage));
        } catch (ExecutionException e) {
            fLogger.warning(e);
            formattedMessage = replace(contextMessage);
        }

        messageContext.setMessage(formattedMessage);
    }

    public void addTags(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;

        UUID processId = messageContext.getMessageUUID();
        FPlayer receiver = messageContext.getReceiver();
        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.MENTION, (argumentQueue, context) -> {
            Tag.Argument mentionTag = argumentQueue.peek();
            if (mentionTag == null) return Tag.selfClosingInserting(Component.empty());

            String mention = mentionTag.value();
            if (mention.isEmpty()) {
                return Tag.preProcessParsed(config().getTrigger() + mention);
            }

            Optional<String> group = findGroup(mention);
            if (group.isPresent()) {
                if (permissionChecker.check(sender, permission().getGroup() + "." + group.get())) {
                    sendMention(processId, receiver);
                    return mentionTag(sender, receiver, mention);
                }
            } else {
                FPlayer mentionFPlayer = fPlayerService.getFPlayer(mention);
                if (!mentionFPlayer.isUnknown() && mentionFPlayer.isOnline() && integrationModule.canSeeVanished(mentionFPlayer, sender)) {
                    sendMention(processId, mentionFPlayer);
                    return mentionTag(sender, receiver, mention);
                }
            }

            return Tag.preProcessParsed(config().getTrigger() + mention);
        });
    }

    private Tag mentionTag(FEntity sender, FPlayer receiver, String mention) {
        String format = StringUtils.replaceEach(localization(receiver).getFormat(),
                new String[]{ "<player>", "<target>" },
                new String[]{ mention, mention }
        );

        return Tag.selfClosingInserting(messagePipeline.builder(sender, receiver, format).build());
    }

    private String replace(String message) {
        String[] words = message.split(" ");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.startsWith(config().getTrigger())) continue;

            String wordWithoutPrefix = Strings.CS.replaceOnce(word, config().getTrigger(), "");
            if (isMention(wordWithoutPrefix)) {
                words[i] = "<mention:" + wordWithoutPrefix + ">";
            }
        }

        return String.join(" ", words);
    }

    private boolean isMention(String word) {
        if (StringUtils.isEmpty(word)) return false;

        Optional<String> group = findGroup(word);
        if (group.isPresent()) {
            return true;
        }

        FPlayer mentionFPlayer = fPlayerService.getFPlayer(word);
        return !mentionFPlayer.isUnknown();
    }

    private Optional<String> findGroup(String group) {
        if (config().getEveryoneTag().equalsIgnoreCase(group)) {
            group = "default";
        }

        String finalGroup = group;
        return integrationModule.getGroups()
                .stream()
                .filter(string -> string.equalsIgnoreCase(finalGroup))
                .findFirst();
    }

    private void sendMention(UUID processId, FPlayer fPlayer) {
        if (permissionChecker.check(fPlayer, permission().getBypass())) return;
        if (processedMentions.containsKey(processId)) return;

        processedMentions.put(processId, true);

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .format(Localization.Message.Format.Mention::getPerson)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}
