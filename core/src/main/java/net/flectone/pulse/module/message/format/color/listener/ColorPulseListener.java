package net.flectone.pulse.module.message.format.color.listener;

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
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.color.ColorModule;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class ColorPulseListener implements PulseListener {

    private final Message.Format.Color message;
    private final Permission.Message.Format formatPermission;
    private final ColorModule colorModule;
    private final PermissionChecker permissionChecker;

    @Inject
    public ColorPulseListener(FileResolver fileResolver,
                              ColorModule colorModule,
                              PermissionChecker permissionChecker) {
        this.message = fileResolver.getMessage().getFormat().getColor();
        this.formatPermission = fileResolver.getPermission().getMessage().getFormat();
        this.colorModule = colorModule;
        this.permissionChecker = permissionChecker;
    }

    @Pulse(priority = Event.Priority.HIGH)
    public void onMessageProcessingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        FEntity sender = messageContext.getSender();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE) && !permissionChecker.check(sender, formatPermission.getAll())) return;
        if (colorModule.checkModulePredicates(sender)) return;

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

}
