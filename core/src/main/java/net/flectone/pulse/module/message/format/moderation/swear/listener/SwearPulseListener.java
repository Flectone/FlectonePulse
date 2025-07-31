package net.flectone.pulse.module.message.format.moderation.swear.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.moderation.swear.SwearModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.resolver.FileResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.util.regex.Matcher;

@Singleton
public class SwearPulseListener implements PulseListener {

    private final Message.Format.Moderation.Swear message;
    private final Permission.Message.Format.Moderation.Swear permission;
    private final SwearModule swearModule;
    private final MessagePipeline messagePipeline;
    private final PermissionChecker permissionChecker;

    @Inject
    public SwearPulseListener(FileResolver fileResolver,
                              SwearModule swearModule,
                              MessagePipeline messagePipeline,
                              PermissionChecker permissionChecker) {
        this.message = fileResolver.getMessage().getFormat().getModeration().getSwear();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getModeration().getSwear();
        this.swearModule = swearModule;
        this.messagePipeline = messagePipeline;
        this.permissionChecker = permissionChecker;
    }

    @Pulse
    public void onMessageProcessingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.SWEAR)) return;

        FEntity sender = messageContext.getSender();
        if (swearModule.checkModulePredicates(sender)) return;
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        String processedMessage = replace(messageContext.getSender(), messageContext.getMessage());
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

    private String replace(FEntity sender, String string) {
        if (permissionChecker.check(sender, permission.getBypass())) return string;
        if (swearModule.getCombinedPattern() == null) return string;

        StringBuilder result = new StringBuilder();
        Matcher matcher = swearModule.getCombinedPattern().matcher(string);
        while (matcher.find()) {
            String word = matcher.group(0);
            if (word != null && message.getIgnore().contains(word.trim().toLowerCase())) continue;

            matcher.appendReplacement(result, "<swear:'" + word + "'>");
        }

        matcher.appendTail(result);

        return result.toString();
    }
}
