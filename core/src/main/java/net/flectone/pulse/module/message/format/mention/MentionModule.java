package net.flectone.pulse.module.message.format.mention;

import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.mention.listener.MentionPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.file.FileFacade;
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
    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final FPlayerService fPlayerService;
    private final IntegrationModule integrationModule;
    private final PermissionChecker permissionChecker;
    private final MessagePipeline messagePipeline;
    private final FLogger fLogger;

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(MentionPulseListener.class);
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder().add(permission().sound(), permission().group(), permission().bypass());
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
        return fileFacade.message().format().mention();
    }

    @Override
    public Permission.Message.Format.Mention permission() {
        return fileFacade.permission().message().format().mention();
    }

    @Override
    public Localization.Message.Format.Mention localization(FEntity sender) {
        return fileFacade.localization(sender).message().format().mention();
    }

    public MessageContext format(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (isModuleDisabledFor(sender)) return messageContext;
        if (isUnknownSender(sender)) return messageContext;

        String contextMessage = messageContext.message();
        if (StringUtils.isEmpty(contextMessage)) return messageContext;

        String formattedMessage;
        try {
            formattedMessage = messageCache.get(contextMessage, () -> replace(contextMessage));
        } catch (ExecutionException e) {
            fLogger.warning(e);
            formattedMessage = replace(contextMessage);
        }

        return messageContext.withMessage(formattedMessage);
    }

    public MessageContext addTags(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (isModuleDisabledFor(sender)) return messageContext;

        UUID processId = messageContext.messageUUID();
        FPlayer receiver = messageContext.receiver();
        return messageContext.addTagResolver(MessagePipeline.ReplacementTag.MENTION, (argumentQueue, context) -> {
            Tag.Argument mentionTag = argumentQueue.peek();
            if (mentionTag == null) return Tag.selfClosingInserting(Component.empty());

            String mention = mentionTag.value();
            if (mention.isEmpty()) {
                return Tag.preProcessParsed(config().trigger() + mention);
            }

            Optional<String> group = findGroup(mention);
            if (group.isPresent()) {
                if (permissionChecker.check(sender, permission().group().name() + "." + group.get())) {
                    sendMention(processId, receiver);
                    return mentionTag(sender, receiver, mention);
                }
            } else {
                FPlayer mentionFPlayer = fPlayerService.getFPlayer(mention);
                if (!mentionFPlayer.isUnknown() && mentionFPlayer.isOnline() && integrationModule.canSeeVanished(mentionFPlayer, sender)) {
                    if (mentionFPlayer.equals(receiver)) {
                        sendMention(processId, mentionFPlayer);
                    }

                    return mentionTag(sender, receiver, mention);
                }
            }

            return Tag.preProcessParsed(config().trigger() + mention);
        });
    }

    private boolean isUnknownSender(FEntity sender) {
        if (!sender.isUnknown()) return false;
        if (!(sender instanceof FPlayer fPlayer)) return false;

        // console - unknown player, but known sender
        return !fPlayer.isConsole();
    }

    private Tag mentionTag(FEntity sender, FPlayer receiver, String mention) {
        String format = StringUtils.replaceEach(localization(receiver).format(),
                new String[]{ "<player>", "<target>" },
                new String[]{ mention, mention }
        );

        MessageContext context = messagePipeline.createContext(sender, receiver, format);
        Component component = messagePipeline.build(context);
        return Tag.selfClosingInserting(component);
    }

    private String replace(String message) {
        String[] words = message.split(" ");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.startsWith(config().trigger())) continue;

            String wordWithoutPrefix = Strings.CS.replaceOnce(word, config().trigger(), "");
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
        if (config().everyoneTag().equalsIgnoreCase(group)) {
            group = "default";
        }

        String finalGroup = group;
        return integrationModule.getGroups()
                .stream()
                .filter(string -> string.equalsIgnoreCase(finalGroup))
                .findFirst();
    }

    public void sendMention(UUID processId, FPlayer fPlayer) {
        if (permissionChecker.check(fPlayer, permission().bypass())) return;
        if (processedMentions.containsKey(processId)) return;

        processedMentions.put(processId, true);

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .format(Localization.Message.Format.Mention::person)
                .destination(config().destination())
                .sound(soundOrThrow())
                .build()
        );
    }
}
