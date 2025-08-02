package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.SenderToReceiverMessageEvent;
import net.flectone.pulse.module.integration.BukkitIntegrationModule;
import net.kyori.adventure.text.Component;

@Singleton
public class BukkitMessageListener implements PulseListener {

    private final BukkitIntegrationModule integrationModule;

    @Inject
    public BukkitMessageListener(BukkitIntegrationModule integrationModule) {
        this.integrationModule = integrationModule;
    }


    @Pulse(priority = Event.Priority.HIGH)
    public void onSenderToReceiverMessageEvent(SenderToReceiverMessageEvent event) {
        if (event.getDestination().getType() != Destination.Type.CHAT) return;

        FPlayer fPlayer = event.getReceiver();
        Component message = event.getMessage();
        boolean sent = integrationModule.sendMessageWithInteractiveChat(fPlayer, message);
        event.setCancelled(sent);
    }
}
