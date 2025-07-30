package net.flectone.pulse.module.message.format.moderation.swear;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Singleton
public class SwearModule extends AbstractModuleMessage<Localization.Message.Format.Moderation.Swear> implements MessageProcessor {


    private final Message.Format.Moderation.Swear message;
    private final Permission.Message.Format.Moderation.Swear permission;
    private final PermissionChecker permissionChecker;
    private final FLogger fLogger;
    private final MessagePipeline messagePipeline;
    private final MessageProcessRegistry messageProcessRegistry;

    private Pattern combinedPattern;

    @Inject
    public SwearModule(FileResolver fileResolver,
                       PermissionChecker permissionChecker,
                       FLogger fLogger,
                       MessagePipeline messagePipeline,
                       MessageProcessRegistry messageProcessRegistry) {
        super(localization -> localization.getMessage().getFormat().getModeration().getSwear());

        this.message = fileResolver.getMessage().getFormat().getModeration().getSwear();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getModeration().getSwear();
        this.permissionChecker = permissionChecker;
        this.fLogger = fLogger;
        this.messagePipeline = messagePipeline;
        this.messageProcessRegistry = messageProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        registerPermission(permission.getBypass());
        registerPermission(permission.getSee());

        try {
            combinedPattern = Pattern.compile(String.join("|", this.message.getTrigger()));
        } catch (PatternSyntaxException e) {
            fLogger.warning(e);
        }

        messageProcessRegistry.register(100, this);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        if (!messageContext.isFlag(MessageFlag.SWEAR)) return;

        FEntity sender = messageContext.getSender();
        if (checkModulePredicates(sender)) return;

        FPlayer receiver = messageContext.getReceiver();
        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.SWEAR, (argumentQueue, context) -> {
            Tag.Argument swearTag = argumentQueue.peek();
            if (swearTag == null) return Tag.selfClosingInserting(Component.empty());

            String swear = swearTag.value();
            if (swear.isBlank()) return Tag.selfClosingInserting(Component.empty());

            String message = resolveLocalization(receiver).getSymbol().repeat(swear.length());

            Component component = messagePipeline.builder(sender, receiver, message).build();

            if (permissionChecker.check(receiver, permission.getSee())) {
                component = component.hoverEvent(HoverEvent.showText(Component.text(swear)));
            }

            return Tag.selfClosingInserting(component);
        });

        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        String processedMessage = replace(messageContext.getSender(), messageContext.getMessage());
        messageContext.setMessage(processedMessage);
    }

    private String replace(FEntity sender, String message) {
        if (permissionChecker.check(sender, permission.getBypass())) return message;
        if (combinedPattern == null) return message;

        StringBuilder result = new StringBuilder();
        Matcher matcher = combinedPattern.matcher(message);
        while (matcher.find()) {
            String word = matcher.group(0);
            if (word != null && this.message.getIgnore().contains(word.trim().toLowerCase())) continue;

            matcher.appendReplacement(result, "<swear:'" + word + "'>");
        }

        matcher.appendTail(result);

        return result.toString();
    }
}
