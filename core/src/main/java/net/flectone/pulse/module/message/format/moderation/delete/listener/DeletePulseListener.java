package net.flectone.pulse.module.message.format.moderation.delete.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.SenderToReceiverMessageEvent;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.module.message.format.moderation.delete.DeleteModule;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.util.UUID;

@Singleton
public class DeletePulseListener implements PulseListener {

    private final DeleteModule deleteModule;
    private final MessagePipeline messagePipeline;

    @Inject
    public DeletePulseListener(DeleteModule deleteModule,
                               MessagePipeline messagePipeline) {
        this.deleteModule = deleteModule;
        this.messagePipeline = messagePipeline;
    }

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;
        if (!messageContext.isFlag(MessageFlag.DELETE)) return;

        FEntity sender = messageContext.getSender();
        FPlayer receiver = messageContext.getReceiver();
        if (deleteModule.isModuleDisabledFor(receiver)) return;

        String message = messageContext.getMessage();
        if (message == null || !message.contains("<delete>")) return;

        UUID messageUUID = messageContext.getMessageUUID();

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.DELETE, (argumentQueue, context) -> {
            String placeholder = deleteModule.resolveLocalization(receiver)
                    .getPlaceholder()
                    .replace("<uuid>", messageUUID.toString());

            Component componentPlaceholder = messagePipeline.builder(sender, receiver, placeholder)
                    .flag(MessageFlag.MENTION, false)
                    .flag(MessageFlag.INTERACTIVE_CHAT, false)
                    .flag(MessageFlag.QUESTION, false)
                    .flag(MessageFlag.DELETE, false)
                    .build();

            return Tag.selfClosingInserting(componentPlaceholder);
        });
    }

    @Pulse(priority = Event.Priority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        FPlayer fPlayer = event.getPlayer();
        deleteModule.clearHistory(fPlayer);
    }

    @Pulse(priority = Event.Priority.MONITOR)
    public void onTranslatableMessageReceiveEvent(TranslatableMessageReceiveEvent event) {
        // save only unknown messages for FlectonePulse
        if (event.getKey() != MinecraftTranslationKey.UNKNOWN) return;

        FPlayer fReceiver = event.getFPlayer();
        UUID messageUUID = UUID.randomUUID();
        Component component = event.getComponent();

        deleteModule.save(fReceiver, messageUUID, component);
    }

    @Pulse(priority = Event.Priority.MONITOR)
    public void onSenderToReceiverMessageEvent(SenderToReceiverMessageEvent event) {
        if (event.getDestination().getType() != Destination.Type.CHAT) return;

        FPlayer fReceiver = event.getReceiver();
        UUID messageUUID = event.getMessageUUID();
        Component component = event.getMessage();

        deleteModule.save(fReceiver, messageUUID, component);
    }

}
