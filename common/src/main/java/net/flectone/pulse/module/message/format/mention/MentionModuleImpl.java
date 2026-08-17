package net.flectone.pulse.module.message.format.mention;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.mention.listener.PulseMentionListener;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MentionModuleImpl implements MentionModule {

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

    private Pattern mentionPattern;

    @Override
    public void onEnable() {
        try {
            mentionPattern = Pattern.compile(Strings.CS.replace(config().namePattern(), "<trigger>", Pattern.quote(config().trigger())));
        } catch (PatternSyntaxException e) {
            fLogger.warning(e);
        }

        listenerRegistry.register(PulseMentionListener.class);
    }

    @Override
    public Set<PermissionSetting> permissions() {
        Set<PermissionSetting> permissions = new LinkedHashSet<>(MentionModule.super.permissions());
        permissions.add(permission().sound());
        permissions.add(permission().group());
        permissions.add(permission().bypass());
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public void onDisable() {
        mentionPattern = null;
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

    @Override
    public MessageContext format(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;
        if (isUnknownSender(sender)) return messageContext;

        String contextMessage = messageContext.message();
        if (StringUtils.isEmpty(contextMessage)) return messageContext;

        return messageContext.withMessage(messageCache.get(contextMessage, _ -> replace(contextMessage)));
    }

    @Override
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
                if (!mentionFPlayer.isUnknown()) {
                    if (mentionFPlayer.equals(receiver) && socialService.canSeeVanished(mentionFPlayer, sender)) {
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
        if (mentionPattern == null) return message;

        Matcher matcher = mentionPattern.matcher(message);
        if (matcher.groupCount() == 0) return message;

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(1);
            if (isMention(name)) {
                matcher.appendReplacement(result, Matcher.quoteReplacement("<mention:" + name + ">"));
            }
        }

        return matcher.appendTail(result).toString();
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

    @Override
    public void sendMention(FPlayer fPlayer) {
        if (permissionChecker.check(fPlayer, permission().bypass())) return;

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .destination(config().destination())
                .sound(soundOrThrow())
                .messageContext(fResolver -> MessageContext.builder()
                        .sender(fPlayer)
                        .receiver(fResolver)
                        .message(localization(fResolver).person())
                        .build()
                )
                .build()
        );
    }
}
