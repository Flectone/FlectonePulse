package net.flectone.pulse.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.SenderToReceiverMessageEvent;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.sender.MessageSender;
import net.kyori.adventure.text.Component;

@Singleton
public class MessagePulseListener implements PulseListener {

    private final MessageSender messageSender;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Inject
    public MessagePulseListener(MessageSender messageSender,
                                PlatformPlayerAdapter platformPlayerAdapter) {
        this.messageSender = messageSender;
        this.platformPlayerAdapter = platformPlayerAdapter;
    }

    @Pulse(priority = Event.Priority.HIGHEST)
    public void onSenderToReceiverMessageEvent(SenderToReceiverMessageEvent event) {
        Component message = event.getMessage();
        if (!Component.IS_NOT_EMPTY.test(message)) return;

        FPlayer fReceiver = event.getReceiver();
        Component submessage = event.getSubmessage();
        Destination destination = event.getDestination();
        switch (destination.getType()) {
            case TITLE -> messageSender.sendTitle(fReceiver, message, submessage, destination.getTimes());
            case SUBTITLE -> messageSender.sendTitle(fReceiver, submessage, message, destination.getTimes());
            case ACTION_BAR -> messageSender.sendActionBar(fReceiver, message, destination.getTimes().stayTicks());
            case BOSS_BAR -> messageSender.sendBoosBar(fReceiver, message, destination.getBossBar());
            case TAB_HEADER -> messageSender.sendPlayerListHeaderAndFooter(fReceiver, message, platformPlayerAdapter.getPlayerListFooter(fReceiver));
            case TAB_FOOTER -> messageSender.sendPlayerListHeaderAndFooter(fReceiver, platformPlayerAdapter.getPlayerListHeader(fReceiver), message);
            case TOAST -> messageSender.sendToast(fReceiver, message, destination.getToast());
            case BRAND -> messageSender.sendBrand(fReceiver, message);
            default -> messageSender.sendMessage(fReceiver, message);
        }
    }
}
