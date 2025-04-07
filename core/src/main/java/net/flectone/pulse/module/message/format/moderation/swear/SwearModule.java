package net.flectone.pulse.module.message.format.moderation.swear;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.formatter.MessageFormatter;
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
public class SwearModule extends AbstractModuleMessage<Localization.Message.Format.Moderation.Swear> {


    private final Message.Format.Moderation.Swear message;
    private final Permission.Message.Format.Moderation.Swear permission;

    private final PermissionChecker permissionChecker;
    private final FLogger fLogger;
    private final MessageFormatter messageFormatter;

    private Pattern combinedPattern;

    @Inject
    public SwearModule(FileManager fileManager,
                       PermissionChecker permissionChecker,
                       FLogger fLogger,
                       MessageFormatter messageFormatter) {
        super(localization -> localization.getMessage().getFormat().getModeration().getSwear());

        this.permissionChecker = permissionChecker;
        this.fLogger = fLogger;
        this.messageFormatter = messageFormatter;

        message = fileManager.getMessage().getFormat().getModeration().getSwear();
        permission = fileManager.getPermission().getMessage().getFormat().getModeration().getSwear();
    }

    @Override
    public void reload() {
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
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    public String replace(FEntity sender, String message) {
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

    public TagResolver swearTag(FEntity sender, FEntity receiver) {
        String tag = "swear";
        if (checkModulePredicates(sender)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            Tag.Argument swearTag = argumentQueue.peek();
            if (swearTag == null) return Tag.selfClosingInserting(Component.empty());

            String swear = swearTag.value();
            if (swear.isBlank()) return Tag.selfClosingInserting(Component.empty());

            String message = resolveLocalization(receiver).getSymbol().repeat(swear.length());

            Component component = messageFormatter.builder(sender, receiver, message).build();

            if (permissionChecker.check(receiver, permission.getSee())) {
                component = component.hoverEvent(HoverEvent.showText(Component.text(swear)));
            }

            return Tag.selfClosingInserting(component);
        });
    }
}
