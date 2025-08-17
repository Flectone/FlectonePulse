package net.flectone.pulse.module.message.format.moderation.delete.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.model.event.message.SenderToReceiverMessageEvent;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.module.message.format.moderation.delete.DeleteModule;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.Component;

import java.util.UUID;

@Singleton
public class DeletePulseListener implements PulseListener {

    private final DeleteModule deleteModule;

    @Inject
    public DeletePulseListener(DeleteModule deleteModule) {
        this.deleteModule = deleteModule;
    }

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.DELETE)) return;

        deleteModule.addTag(messageContext);
    }

    @Pulse(priority = Event.Priority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        FPlayer fPlayer = event.getPlayer();
        deleteModule.clearHistory(fPlayer);
    }

    @Pulse(priority = Event.Priority.MONITOR)
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        // skip action bar messages
        if (event.isOverlay()) return;

        Component component = event.getComponent();

        // skip FlectonePulse messages
        if (event.getTranslationKey() != MinecraftTranslationKey.UNKNOWN) return;
        if (deleteModule.isCached(component)) {
            deleteModule.removeCache(component);
            return;
        }

        FPlayer fReceiver = event.getFPlayer();
        UUID messageUUID = UUID.randomUUID();

        deleteModule.save(fReceiver, messageUUID, component, false);
    }

    @Pulse(priority = Event.Priority.MONITOR)
    public void onSenderToReceiverMessageEvent(SenderToReceiverMessageEvent event) {
        if (event.getDestination().getType() != Destination.Type.CHAT) return;

        FPlayer fReceiver = event.getReceiver();
        UUID messageUUID = event.getMessageUUID();
        Component component = event.getMessage();

        deleteModule.save(fReceiver, messageUUID, component, true);
    }

}
