package net.flectone.pulse.module.integration.itemsadder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import lombok.Getter;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Singleton
public class ItemsAdderIntegration implements FIntegration, PulseListener {

    private final FLogger fLogger;

    @Getter
    private boolean hooked;

    @Inject
    public ItemsAdderIntegration(FLogger fLogger) {
        this.fLogger = fLogger;
    }

    @Override
    public void hook() {
        hooked = true;
        fLogger.info("✔ ItemsAdder hooked");
    }

    @Override
    public void unhook() {
        hooked = false;
        fLogger.info("✖ ItemsAdder unhooked");
    }

    @Pulse(priority = Event.Priority.LOW)
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!isHooked()) return;

        FEntity fPlayer = messageContext.getSender();
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        String message = FontImageWrapper.replaceFontImages(player, messageContext.getMessage());
        messageContext.setMessage(message);
    }
}
