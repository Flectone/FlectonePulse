package net.flectone.pulse.module.message.format.moderation.swear;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.PermissionUtil;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Singleton
public class SwearModule extends AbstractModuleMessage<Localization.Message.Format.Moderation.Swear> {


    private final Message.Format.Moderation.Swear message;
    private final Permission.Message.Format.Moderation.Swear permission;

    private final PermissionUtil permissionUtil;
    private final FLogger fLogger;

    @Inject private ComponentUtil componentUtil;

    private Pattern combinedPattern;

    @Inject
    public SwearModule(FileManager fileManager,
                       PermissionUtil permissionUtil,
                       FLogger fLogger) {
        super(localization -> localization.getMessage().getFormat().getModeration().getSwear());

        this.permissionUtil = permissionUtil;
        this.fLogger = fLogger;

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
        if (permissionUtil.has(sender, permission.getBypass())) return message;
        if (combinedPattern == null) return message;

        StringBuilder result = new StringBuilder();
        Matcher matcher = combinedPattern.matcher(message);
        while (matcher.find()) {

            matcher.appendReplacement(result, "<swear:\"" + matcher.group(0) + "\">");
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public TagResolver swearTag(FEntity sender, FEntity receiver) {
        if (checkModulePredicates(sender)) return TagResolver.empty();

        return TagResolver.resolver("swear", (argumentQueue, context) -> {
            Tag.Argument swearTag = argumentQueue.peek();
            if (swearTag == null) return Tag.selfClosingInserting(Component.empty());

            String swear = swearTag.value();
            if (swear.isBlank()) return Tag.selfClosingInserting(Component.empty());

            String message = resolveLocalization(receiver).getSymbol().repeat(swear.length());

            Component component = componentUtil.builder(sender, receiver, message).build();

            if (permissionUtil.has(receiver, permission.getSee())) {
                component = component.hoverEvent(HoverEvent.showText(Component.text(swear)));
            }

            return Tag.selfClosingInserting(component);
        });
    }
}
