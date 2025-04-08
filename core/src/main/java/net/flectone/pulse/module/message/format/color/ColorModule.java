package net.flectone.pulse.module.message.format.color;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.HashMap;
import java.util.Map;

import static net.flectone.pulse.util.TagResolverUtil.emptyTagResolver;


@Singleton
public class ColorModule extends AbstractModule implements MessageProcessor {

    @Getter private final Message.Format.Color message;
    private final Permission.Message.Format.Color permission;
    private final Permission.Message.Format formatPermission;

    private final PermissionChecker permissionChecker;

    @Inject
    public ColorModule(FileManager fileManager,
                       PermissionChecker permissionChecker,
                       MessageProcessRegistry messageProcessRegistry) {
        this.permissionChecker = permissionChecker;

        message = fileManager.getMessage().getFormat().getColor();
        permission = fileManager.getPermission().getMessage().getFormat().getColor();
        formatPermission = fileManager.getPermission().getMessage().getFormat();

        messageProcessRegistry.register(150, this);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    @Override
    public void process(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (messageContext.isUserMessage() && !permissionChecker.check(sender, formatPermission.getAll())) return;

        messageContext.addTagResolvers(colorTag(message.isUseRecipientColors() ? messageContext.getReceiver() : sender));
    }

    private TagResolver colorTag(FEntity sender) {
        String tag = "fcolor";
        if (checkModulePredicates(sender)) return emptyTagResolver(tag);

        Map<String, String> playerColors = sender instanceof FPlayer fPlayer
                ? fPlayer.getColors()
                : new HashMap<>();

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            Tag.Argument colorArg = argumentQueue.peek();
            if (colorArg == null) return Tag.selfClosingInserting(Component.empty());

            String number = colorArg.value();
            if (!playerColors.containsKey(number) && !message.getValues().containsKey(number)) {
                return Tag.inserting(Component.empty());
            }

            String color = playerColors.getOrDefault(number, message.getValues().get(number));

            return Tag.inserting(Component.empty().color(TextColor.fromHexString(color)));
        });
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }
}
