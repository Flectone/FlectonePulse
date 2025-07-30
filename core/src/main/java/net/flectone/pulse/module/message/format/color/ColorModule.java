package net.flectone.pulse.module.message.format.color;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.util.HashMap;
import java.util.Map;


@Singleton
public class ColorModule extends AbstractModule implements MessageProcessor {

    @Getter private final Message.Format.Color message;
    private final Permission.Message.Format.Color permission;
    private final Permission.Message.Format formatPermission;
    private final PermissionChecker permissionChecker;
    private final MessageProcessRegistry messageProcessRegistry;

    @Inject
    public ColorModule(FileResolver fileResolver,
                       PermissionChecker permissionChecker,
                       MessageProcessRegistry messageProcessRegistry) {
        this.message = fileResolver.getMessage().getFormat().getColor();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getColor();
        this.formatPermission = fileResolver.getPermission().getMessage().getFormat();
        this.permissionChecker = permissionChecker;
        this.messageProcessRegistry = messageProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        messageProcessRegistry.register(150, this);
    }

    @Override
    public void process(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (messageContext.isUserMessage() && !permissionChecker.check(sender, formatPermission.getAll())) return;
        if (checkModulePredicates(sender)) return;

        Map<String, String> playerColors = sender instanceof FPlayer fPlayer
                ? fPlayer.getColors()
                : new HashMap<>();

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.FCOLOR, (argumentQueue, context) -> {
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
    protected boolean isConfigEnable() {
        return message.isEnable();
    }
}
