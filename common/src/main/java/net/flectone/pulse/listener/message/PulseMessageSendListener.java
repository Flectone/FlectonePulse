package net.flectone.pulse.listener.message;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.lifecycle.ReloadEvent;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.model.value.Destination;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.render.*;
import net.flectone.pulse.platform.sender.MessageSender;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.kyori.adventure.text.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseMessageSendListener implements PulseListener {

    private static final Set<Destination.Type> PERSISTENT_DESTINATIONS = EnumSet.of(
            Destination.Type.TAB_HEADER,
            Destination.Type.TAB_FOOTER,
            Destination.Type.BRAND
    );

    private final Map<UUID, Map<Destination.Type, Integer>> sentMessages = new ConcurrentHashMap<>();

    private final SoundPlayer soundPlayer;
    private final MessageSender messageSender;
    private final ActionBarRender actionBarRender;
    private final BossBarRender bossBarRender;
    private final BrandRender brandRender;
    private final ListFooterRender listFooterRender;
    private final TextScreenRender textScreenRender;
    private final TitleRender titleRender;
    private final ToastRender toastRender;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Pulse(priority = Event.Priority.MONITOR)
    public void onMessageSendEvent(MessageSendEvent event) {
        EventMetadata eventMetadata = event.eventMetadata();
        if (eventMetadata.sound() != null) {
            soundPlayer.play(eventMetadata.sound(), event.messageContext().sender(), event.messageContext().receiver());
        }

        Component message = event.message();
        if (!Component.IS_NOT_EMPTY.test(message)) return;

        FPlayer fReceiver = event.messageContext().receiver();

        Destination destination = event.eventMetadata().destination();
        if (isAlreadySent(fReceiver, destination.type(), message)) return;

        if (fReceiver.isConsole() && destination.type() != Destination.Type.CHAT) {
            messageSender.sendToConsole(message);
            return;
        }

        switch (destination.type()) {
            case TITLE -> titleRender.render(fReceiver, message, event.submessage(), destination.times());
            case SUBTITLE -> titleRender.render(fReceiver, event.submessage(), message, destination.times());
            case ACTION_BAR -> actionBarRender.render(fReceiver, message, destination.times().stayTicks());
            case BOSS_BAR -> bossBarRender.render(fReceiver, message, destination.bossBar());
            case TAB_HEADER -> listFooterRender.render(fReceiver, message, platformPlayerAdapter.getPlayerListFooter(fReceiver));
            case TAB_FOOTER -> listFooterRender.render(fReceiver, platformPlayerAdapter.getPlayerListHeader(fReceiver), message);
            case TOAST -> toastRender.render(fReceiver, message, event.submessage(), destination.toast());
            case BRAND -> brandRender.render(fReceiver, message);
            case TEXT_SCREEN -> textScreenRender.render(fReceiver, message, destination.textScreen());
            default -> messageSender.sendMessage(fReceiver, message, false);
        }
    }

    @Pulse
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        sentMessages.remove(event.player().uuid());
    }

    @Pulse
    public void onReloadEvent(ReloadEvent event) {
        if (event.type() != ReloadEvent.Type.END) return;

        sentMessages.clear();
    }

    private boolean isAlreadySent(FPlayer fReceiver, Destination.Type destinationType, Component message) {
        if (!PERSISTENT_DESTINATIONS.contains(destinationType)) return false;

        int hash = message.hashCode();
        Integer sentMessage = sentMessages.computeIfAbsent(fReceiver.uuid(), _ -> new ConcurrentHashMap<>())
                .put(destinationType, hash);

        return sentMessage != null && sentMessage == hash;
    }

}
