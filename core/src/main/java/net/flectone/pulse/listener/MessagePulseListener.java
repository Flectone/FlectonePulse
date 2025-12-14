package net.flectone.pulse.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.render.*;
import net.flectone.pulse.platform.sender.MessageSender;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessagePulseListener implements PulseListener {

    private final MessageSender messageSender;
    private final ActionBarRender actionBarRender;
    private final BossBarRender bossBarRender;
    private final BrandRender brandRender;
    private final ListFooterRender listFooterRender;
    private final TextScreenRender textScreenRender;
    private final TitleRender titleRender;
    private final ToastRender toastRender;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final @Named("isNewerThanOrEqualsV_1_19_4") boolean isNewerThanOrEqualsV_1_19_4;

    @Pulse(priority = Event.Priority.HIGHEST)
    public void onSenderToReceiverMessageEvent(MessageSendEvent event) {
        Component message = event.getMessage();
        if (!Component.IS_NOT_EMPTY.test(message)) return;

        FPlayer fReceiver = event.getReceiver();

        Destination destination = event.getEventMetadata().getDestination();

        // fallback for legacy versions
        if (destination.getType() == Destination.Type.TEXT_SCREEN && !isNewerThanOrEqualsV_1_19_4) {
            destination = new Destination(Destination.Type.TITLE);
        }

        if (fReceiver.isConsole() && destination.getType() != Destination.Type.CHAT) {
            messageSender.sendToConsole(message);
            return;
        }

        switch (destination.getType()) {
            case TITLE -> titleRender.render(fReceiver, message, event.getSubmessage(), destination.getTimes());
            case SUBTITLE -> titleRender.render(fReceiver, event.getSubmessage(), message, destination.getTimes());
            case ACTION_BAR -> actionBarRender.render(fReceiver, message, destination.getTimes().stayTicks());
            case BOSS_BAR -> bossBarRender.render(fReceiver, message, destination.getBossBar());
            case TAB_HEADER -> listFooterRender.render(fReceiver, message, platformPlayerAdapter.getPlayerListFooter(fReceiver));
            case TAB_FOOTER -> listFooterRender.render(fReceiver, platformPlayerAdapter.getPlayerListHeader(fReceiver), message);
            case TOAST -> toastRender.render(fReceiver, message, destination.getToast());
            case BRAND -> brandRender.render(fReceiver, message);
            case TEXT_SCREEN -> textScreenRender.render(fReceiver, message, destination.getTextScreen());
            default -> messageSender.sendMessage(fReceiver, message, false);
        }
    }
}
