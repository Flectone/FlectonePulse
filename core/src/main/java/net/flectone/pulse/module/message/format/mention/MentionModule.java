package net.flectone.pulse.module.message.format.mention;

import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.mention.listener.PulseMentionListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MentionModule implements ModuleLocalization<Localization.Message.Format.Mention> {

    private final @Named("mentionMessage") Cache<String, String> messageCache;
    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final FPlayerService fPlayerService;
    private final SocialService socialService;
    private final IntegrationModule integrationModule;
    private final PermissionChecker permissionChecker;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final FLogger fLogger;

    @Override
    public void onEnable() {
        listenerRegistry.register(PulseMentionListener.class);
    }

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleLocalization.super.permissionBuilder().add(permission().sound(), permission().group(), permission().bypass());
    }

    @Override
    public void onDisable() {
        messageCache.invalidateAll();
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_MENTION;
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
    public Localization.Message.Format.Mention localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().format().mention();
    }

    public MessageContext format(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;
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
        if (moduleController.isDisabledFor(this, sender)) return messageContext;

        FPlayer receiver = messageContext.receiver();
        return messageContext.addTagResolver(messagePipeline.resolver(MessagePipeline.ReplacementTag.MENTION.getTagName(), (argumentQueue, _) -> {
            Tag.Argument mentionTag = argumentQueue.peek();
            if (mentionTag == null) return MessagePipeline.ReplacementTag.emptyTag();

            String mention = mentionTag.value();
            if (mention.isEmpty()) {
                return Tag.preProcessParsed(config().trigger() + mention);
            }

            Optional<String> group = findGroup(mention);
            if (group.isPresent()) {
                if (permissionChecker.check(sender, permission().group().name() + "." + group.get())) {
                    sendMention(receiver);

                    return mentionTag(messageContext, mention);
                }
            } else {
                FPlayer mentionFPlayer = fPlayerService.getFPlayer(mention);
                if (!mentionFPlayer.isUnknown() && mentionFPlayer.isOnline() && socialService.canSeeVanished(mentionFPlayer, sender)) {
                    if (mentionFPlayer.equals(receiver)) {
                        sendMention(mentionFPlayer);
                    }

                    return mentionTag(messageContext, mention);
                }
            }

            return Tag.preProcessParsed(config().trigger() + mention);
        }));
    }

    private boolean isUnknownSender(FEntity sender) {
        if (!sender.isUnknown()) return false;
        if (!(sender instanceof FPlayer fPlayer)) return false;

        // console - unknown player, but known sender
        return !fPlayer.isConsole();
    }

    private Tag mentionTag(MessageContext messageContext, String mention) {
        return Tag.selfClosingInserting(messagePipeline.build(MessageContext.builder()
                .sender(messageContext.sender())
                .receiver(messageContext.receiver())
                .message(StringUtils.replaceEach(localization(messageContext.receiver()).format(),
                        new String[]{"<player>", "<target>"},
                        new String[]{mention, mention}
                ))
                .flags(messageContext.flags())
                .flag(MessageFlag.PLAYER_MESSAGE, false)
                .build()
        ));
    }

    private String replace(String message) {
        if (!message.contains(config().trigger())) return message;

        String[] words = message.split(" ", -1);

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
                .findAny();
    }

    public void sendMention(FPlayer fPlayer) {
        if (permissionChecker.check(fPlayer, permission().bypass())) return;

        messageDispatcher.dispatch(this, EventMetadata.<Localization.Message.Format.Mention>builder()
                .sender(fPlayer)
                .format(Localization.Message.Format.Mention::person)
                .destination(config().destination())
                .sound(soundOrThrow())
                .build()
        );
    }
}
