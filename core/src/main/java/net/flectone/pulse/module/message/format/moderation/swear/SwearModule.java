package net.flectone.pulse.module.message.format.moderation.swear;

import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.format.moderation.swear.listener.SwearPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SwearModule extends AbstractModuleLocalization<Localization.Message.Format.Moderation.Swear> {

    private final @Named("swearMessage") Cache<String, String> messageCache;
    private final FileFacade fileFacade;
    private final FLogger fLogger;
    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;
    private final MessagePipeline messagePipeline;

    @Getter private Pattern combinedPattern;

    @Override
    public void onEnable() {
        super.onEnable();

        try {
            combinedPattern = Pattern.compile(String.join("|", config().trigger()));
        } catch (PatternSyntaxException e) {
            fLogger.warning(e);
        }

        listenerRegistry.register(SwearPulseListener.class);
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder().add(permission().see(), permission().bypass());
    }

    @Override
    public void onDisable() {
        super.onDisable();

        messageCache.invalidateAll();
    }

    @Override
    public MessageType messageType() {
        return MessageType.SWEAR;
    }

    @Override
    public Message.Format.Moderation.Swear config() {
        return fileFacade.message().format().moderation().swear();
    }

    @Override
    public Permission.Message.Format.Moderation.Swear permission() {
        return fileFacade.permission().message().format().moderation().swear();
    }

    @Override
    public Localization.Message.Format.Moderation.Swear localization(FEntity sender) {
        return fileFacade.localization(sender).message().format().moderation().swear();
    }

    public MessageContext format(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (isModuleDisabledFor(sender)) return messageContext;
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return messageContext;

        String contextMessage = messageContext.message();
        if (StringUtils.isEmpty(contextMessage)) return messageContext;
        if (permissionChecker.check(sender, permission().bypass())) return messageContext;

        String formattedMessage;
        try {
            formattedMessage = messageCache.get(contextMessage, () -> replace(contextMessage));
        } catch (ExecutionException e) {
            fLogger.warning(e);
            formattedMessage = replace(contextMessage);
        }

        return messageContext.withMessage(formattedMessage);
    }

    public MessageContext addTag(MessageContext messageContext) {
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return messageContext;

        FEntity sender = messageContext.sender();
        if (isModuleDisabledFor(sender)) return messageContext;

        FPlayer receiver = messageContext.receiver();
        return messageContext.addTagResolver(MessagePipeline.ReplacementTag.SWEAR, (argumentQueue, context) -> {
            Tag.Argument swearTag = argumentQueue.peek();
            if (swearTag == null) return Tag.selfClosingInserting(Component.empty());

            String swear = swearTag.value();
            if (swear.isBlank()) return Tag.selfClosingInserting(Component.empty());

            String symbols = localization(receiver).symbol().repeat(swear.length());

            MessageContext tagContext = messagePipeline.createContext(sender, receiver, symbols)
                    .withFlags(messageContext.flags());

            Component component = messagePipeline.build(tagContext);

            if (permissionChecker.check(receiver, permission().see())) {
                component = component.hoverEvent(HoverEvent.showText(Component.text(swear)));
            }

            return Tag.selfClosingInserting(component);
        });
    }

    private String replace(String string) {
        if (combinedPattern == null) return string;

        StringBuilder result = new StringBuilder();
        Matcher matcher = combinedPattern.matcher(string);
        while (matcher.find()) {
            String word = matcher.group(0);
            int start = matcher.start();
            if (isIgnored(word) || isIgnored(getFullWord(string, start))) continue;

            matcher.appendReplacement(result, "<swear:'" + word + "'>");
        }

        matcher.appendTail(result);

        return result.toString();
    }

    private boolean isIgnored(String word) {
        if (StringUtils.isEmpty(word)) return true;
        if (config().ignore().isEmpty()) return false;

        String fullWord = word.trim().toLowerCase(Locale.ROOT);

        return config().ignore().contains(fullWord);
    }

    private String getFullWord(String text, int position) {
        if (position < 0 || position >= text.length()) return text;

        int start = position;
        while (start > 0 && Character.isLetterOrDigit(text.charAt(start - 1))) {
            start--;
        }

        int end = position;
        while (end < text.length() && Character.isLetterOrDigit(text.charAt(end))) {
            end++;
        }

        return text.substring(start, end);
    }
}
