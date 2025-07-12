package net.flectone.pulse.module.message.format.moderation.swear;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static net.flectone.pulse.util.TagResolverUtil.emptyTagResolver;

@Singleton
public class SwearModule extends AbstractModuleMessage<Localization.Message.Format.Moderation.Swear> implements MessageProcessor {


    private final Message.Format.Moderation.Swear message;
    private final Permission.Message.Format.Moderation.Swear permission;

    private final PermissionChecker permissionChecker;
    private final FLogger fLogger;
    private final MessagePipeline messagePipeline;

    private Pattern combinedPattern;

    @Inject
    public SwearModule(FileResolver fileResolver,
                       PermissionChecker permissionChecker,
                       FLogger fLogger,
                       MessagePipeline messagePipeline,
                       MessageProcessRegistry messageProcessRegistry) {
        super(localization -> localization.getMessage().getFormat().getModeration().getSwear());

        this.permissionChecker = permissionChecker;
        this.fLogger = fLogger;
        this.messagePipeline = messagePipeline;

        message = fileResolver.getMessage().getFormat().getModeration().getSwear();
        permission = fileResolver.getPermission().getMessage().getFormat().getModeration().getSwear();

        messageProcessRegistry.register(100, this);
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
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        if (!messageContext.isSwear()) return;

        messageContext.addTagResolvers(swearTag(messageContext.getSender(), messageContext.getReceiver()));

        if (!messageContext.isUserMessage()) return;

        String message = replace(messageContext.getSender(), messageContext.getMessage());
        messageContext.setMessage(message);
    }

    private String replace(FEntity sender, String message) {
        if (checkModulePredicates(sender)) return message;
        if (permissionChecker.check(sender, permission.getBypass())) return message;
        if (combinedPattern == null) return message;

        StringBuilder result = new StringBuilder();
        Matcher matcher = combinedPattern.matcher(message);
        while (matcher.find()) {
            matcher.appendReplacement(result, "<swear:'" + matcher.group(0) + "'>");
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private TagResolver swearTag(FEntity sender, FEntity receiver) {
        String tag = "swear";
        if (checkModulePredicates(sender)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
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
    }
}
