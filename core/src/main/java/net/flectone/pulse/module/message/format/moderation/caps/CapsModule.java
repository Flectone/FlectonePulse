package net.flectone.pulse.module.message.format.moderation.caps;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.MessageProcessRegistry;

@Singleton
public class CapsModule extends AbstractModule implements MessageProcessor {

    private final Message.Format.Moderation.Caps message;
    private final Permission.Message.Format.Moderation.Caps permission;
    private final PermissionChecker permissionChecker;
    private final MessageProcessRegistry messageProcessRegistry;

    @Inject
    public CapsModule(FileResolver fileResolver,
                      PermissionChecker permissionChecker,
                      MessageProcessRegistry messageProcessRegistry) {
        this.message = fileResolver.getMessage().getFormat().getModeration().getCaps();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getModeration().getCaps();
        this.permissionChecker = permissionChecker;
        this.messageProcessRegistry = messageProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        messageProcessRegistry.register(100, this);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        if (!messageContext.isCaps()) return;
        if (!messageContext.isUserMessage()) return;

        String message = replace(messageContext.getSender(), messageContext.getMessage());
        messageContext.setMessage(message);
    }

    private String replace(FEntity sender, String message) {
        if (checkModulePredicates(sender)) return message;
        if (permissionChecker.check(sender, permission.getBypass())) return message;
        if (message == null || message.isEmpty()) return message;

        return needApplyAntiCaps(message) ? message.toLowerCase() : message;
    }

    private boolean needApplyAntiCaps(String message) {
        int uppercaseCount = 0;
        int totalLetters = 0;

        for (char symbol : message.toCharArray()) {
            if (!Character.isLetter(symbol)) continue;

            totalLetters++;

            if (!Character.isUpperCase(symbol)) continue;

            uppercaseCount++;
        }

        return totalLetters > 0 && ((double) uppercaseCount / totalLetters) > this.message.getTrigger();
    }
}
