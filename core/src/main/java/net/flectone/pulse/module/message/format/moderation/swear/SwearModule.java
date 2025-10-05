package net.flectone.pulse.module.message.format.moderation.swear;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.format.moderation.swear.listener.SwearPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Singleton
public class SwearModule extends AbstractModuleLocalization<Localization.Message.Format.Moderation.Swear> {

    private final Cache<String, String> messageCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(100000)
            .build();

    private final FileResolver fileResolver;
    private final FLogger fLogger;
    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;
    private final MessagePipeline messagePipeline;

    @Getter private Pattern combinedPattern;

    @Inject
    public SwearModule(FileResolver fileResolver,
                       FLogger fLogger,
                       ListenerRegistry listenerRegistry,
                       PermissionChecker permissionChecker,
                       MessagePipeline messagePipeline) {
        super(MessageType.SWEAR);

        this.fileResolver = fileResolver;
        this.fLogger = fLogger;
        this.listenerRegistry = listenerRegistry;
        this.permissionChecker = permissionChecker;
        this.messagePipeline = messagePipeline;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        registerPermission(permission().getBypass());
        registerPermission(permission().getSee());

        try {
            combinedPattern = Pattern.compile(String.join("|", config().getTrigger()));
        } catch (PatternSyntaxException e) {
            fLogger.warning(e);
        }

        listenerRegistry.register(SwearPulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        messageCache.invalidateAll();
    }

    @Override
    public Message.Format.Moderation.Swear config() {
        return fileResolver.getMessage().getFormat().getModeration().getSwear();
    }

    @Override
    public Permission.Message.Format.Moderation.Swear permission() {
        return fileResolver.getPermission().getMessage().getFormat().getModeration().getSwear();
    }

    @Override
    public Localization.Message.Format.Moderation.Swear localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getFormat().getModeration().getSwear();
    }

    public void format(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        String contextMessage = messageContext.getMessage();
        if (StringUtils.isEmpty(contextMessage)) return;

        String formattedMessage;
        try {
            formattedMessage = messageCache.get(contextMessage, () -> replace(sender, contextMessage));
        } catch (ExecutionException e) {
            fLogger.warning(e);
            formattedMessage = replace(sender, contextMessage);
        }

        messageContext.setMessage(formattedMessage);
    }

    public void addTag(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        FPlayer receiver = messageContext.getReceiver();
        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.SWEAR, (argumentQueue, context) -> {
            Tag.Argument swearTag = argumentQueue.peek();
            if (swearTag == null) return Tag.selfClosingInserting(Component.empty());

            String swear = swearTag.value();
            if (swear.isBlank()) return Tag.selfClosingInserting(Component.empty());

            String symbols = localization(receiver).getSymbol().repeat(swear.length());

            Component component = messagePipeline.builder(sender, receiver, symbols).build();

            if (permissionChecker.check(receiver, permission().getSee())) {
                component = component.hoverEvent(HoverEvent.showText(Component.text(swear)));
            }

            return Tag.selfClosingInserting(component);
        });
    }

    private String replace(FEntity sender, String string) {
        if (permissionChecker.check(sender, permission().getBypass())) return string;
        if (combinedPattern == null) return string;

        StringBuilder result = new StringBuilder();
        Matcher matcher = combinedPattern.matcher(string);
        while (matcher.find()) {
            String word = matcher.group(0);
            if (word != null && config().getIgnore().contains(word.trim().toLowerCase())) continue;

            matcher.appendReplacement(result, "<swear:'" + word + "'>");
        }

        matcher.appendTail(result);

        return result.toString();
    }
}
