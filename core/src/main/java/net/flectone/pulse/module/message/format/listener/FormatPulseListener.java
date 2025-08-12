package net.flectone.pulse.module.message.format.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.FormatModule;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.AdventureTag;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
public class FormatPulseListener implements PulseListener {

    private final Message.Format message;
    private final Permission.Message.Format permission;
    private final FormatModule formatModule;
    private final PermissionChecker permissionChecker;

    @Inject
    public FormatPulseListener(FileResolver fileResolver,
                               FormatModule formatModule,
                               PermissionChecker permissionChecker) {
        this.message = fileResolver.getMessage().getFormat();
        this.permission = fileResolver.getPermission().getMessage().getFormat();
        this.formatModule = formatModule;
        this.permissionChecker = permissionChecker;
    }

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.ADVENTURE_TAGS)) return;

        FEntity sender = messageContext.getSender();
        if (formatModule.isModuleDisabledFor(sender)) return;

        boolean isUserMessage = messageContext.isFlag(MessageFlag.USER_MESSAGE);

        formatModule.getTagResolverMap()
                .entrySet()
                .stream()
                .filter(entry -> isCorrectTag(entry.getKey(), sender, isUserMessage))
                .forEach(entry -> messageContext.addReplacementTag(entry.getValue()));
    }

    public boolean isCorrectTag(AdventureTag adventureTag, FEntity sender, boolean needPermission) {
        if (!message.getAdventureTags().contains(adventureTag)) return false;
        if (!formatModule.getTagResolverMap().containsKey(adventureTag)) return false;

        return !needPermission || permissionChecker.check(sender, permission.getAdventureTags().get(adventureTag));
    }
}
