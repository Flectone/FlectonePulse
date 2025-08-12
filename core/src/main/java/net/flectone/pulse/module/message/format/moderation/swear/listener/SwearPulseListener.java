package net.flectone.pulse.module.message.format.moderation.swear.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.moderation.swear.SwearModule;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.Tag;

@Singleton
public class SwearPulseListener implements PulseListener {

    private final Permission.Message.Format.Moderation.Swear permission;
    private final SwearModule swearModule;
    private final MessagePipeline messagePipeline;
    private final PermissionChecker permissionChecker;

    @Inject
    public SwearPulseListener(FileResolver fileResolver,
                              SwearModule swearModule,
                              MessagePipeline messagePipeline,
                              PermissionChecker permissionChecker) {
        this.permission = fileResolver.getPermission().getMessage().getFormat().getModeration().getSwear();
        this.swearModule = swearModule;
        this.messagePipeline = messagePipeline;
        this.permissionChecker = permissionChecker;
    }

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.SWEAR)) return;

        FEntity sender = messageContext.getSender();
        if (swearModule.isModuleDisabledFor(sender)) return;
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        String processedMessage = swearModule.cacheReplace(messageContext.getSender(), messageContext.getMessage());
        messageContext.setMessage(processedMessage);

        FPlayer receiver = messageContext.getReceiver();
        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.SWEAR, (argumentQueue, context) -> {
            Tag.Argument swearTag = argumentQueue.peek();
            if (swearTag == null) return Tag.selfClosingInserting(Component.empty());

            String swear = swearTag.value();
            if (swear.isBlank()) return Tag.selfClosingInserting(Component.empty());

            String message = swearModule.resolveLocalization(receiver).getSymbol().repeat(swear.length());

            Component component = messagePipeline.builder(sender, receiver, message).build();

            if (permissionChecker.check(receiver, permission.getSee())) {
                component = component.hoverEvent(HoverEvent.showText(Component.text(swear)));
            }

            return Tag.selfClosingInserting(component);
        });
    }
}
