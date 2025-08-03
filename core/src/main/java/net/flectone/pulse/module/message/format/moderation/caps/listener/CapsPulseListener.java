package net.flectone.pulse.module.message.format.moderation.caps.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.moderation.caps.CapsModule;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class CapsPulseListener implements PulseListener {

    private final Message.Format.Moderation.Caps message;
    private final Permission.Message.Format.Moderation.Caps permission;
    private final CapsModule capsModule;
    private final PermissionChecker permissionChecker;

    @Inject
    public CapsPulseListener(FileResolver fileResolver,
                             CapsModule capsModule,
                             PermissionChecker permissionChecker) {
        this.message = fileResolver.getMessage().getFormat().getModeration().getCaps();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getModeration().getCaps();
        this.capsModule = capsModule;
        this.permissionChecker = permissionChecker;
    }

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.CAPS)) return;
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        String processedMessage = replace(messageContext.getSender(), messageContext.getMessage());
        messageContext.setMessage(processedMessage);
    }

    private String replace(FEntity sender, String string) {
        if (capsModule.isModuleDisabledFor(sender)) return string;
        if (permissionChecker.check(sender, permission.getBypass())) return string;
        if (string == null || string.isEmpty()) return string;

        return needApplyAntiCaps(string) ? string.toLowerCase() : string;
    }

    private boolean needApplyAntiCaps(String string) {
        int uppercaseCount = 0;
        int totalLetters = 0;

        for (char symbol : string.toCharArray()) {
            if (Character.isLetter(symbol)) {
                totalLetters++;
                if (Character.isUpperCase(symbol)) {
                    uppercaseCount++;
                }
            }
        }

        return totalLetters > 0 && ((double) uppercaseCount / totalLetters) > message.getTrigger();
    }
}
