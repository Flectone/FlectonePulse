package net.flectone.pulse.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.sender.MessageSender;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessagePulseListener implements PulseListener {

    private final MessageSender messageSender;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Pulse(priority = Event.Priority.HIGHEST)
    public void onSenderToReceiverMessageEvent(MessageSendEvent event) {
        Component message = event.getMessage();
        if (!Component.IS_NOT_EMPTY.test(message)) return;

        FPlayer fReceiver = event.getReceiver();

        Destination destination = event.getEventMetadata().getDestination();
        if (fReceiver.isConsole() && destination.getType() != Destination.Type.CHAT) {
            messageSender.sendToConsole(message);
            return;
        }

        switch (destination.getType()) {
            case TITLE -> messageSender.sendTitle(fReceiver, message, event.getSubmessage(), destination.getTimes());
            case SUBTITLE -> messageSender.sendTitle(fReceiver, event.getSubmessage(), message, destination.getTimes());
            case ACTION_BAR -> messageSender.sendActionBar(fReceiver, message, destination.getTimes().stayTicks());
            case BOSS_BAR -> messageSender.sendBoosBar(fReceiver, message, destination.getBossBar());
            case TAB_HEADER -> messageSender.sendPlayerListHeaderAndFooter(fReceiver, message, platformPlayerAdapter.getPlayerListFooter(fReceiver));
            case TAB_FOOTER -> messageSender.sendPlayerListHeaderAndFooter(fReceiver, platformPlayerAdapter.getPlayerListHeader(fReceiver), message);
            case TOAST -> messageSender.sendToast(fReceiver, message, destination.getToast());
            case BRAND -> messageSender.sendBrand(fReceiver, message);
            default -> messageSender.sendMessage(fReceiver, message, false);
        }
    }
}
