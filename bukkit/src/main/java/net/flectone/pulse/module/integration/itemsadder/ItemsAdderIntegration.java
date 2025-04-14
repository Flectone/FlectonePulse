package net.flectone.pulse.module.integration.itemsadder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import lombok.Getter;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Singleton
public class ItemsAdderIntegration implements FIntegration, MessageProcessor {

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
        fLogger.info("ItemsAdder hooked");
    }


    @Override
    public void process(MessageContext messageContext) {
        if (!isHooked()) return;

        FEntity fPlayer = messageContext.getSender();
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        String message = FontImageWrapper.replaceFontImages(player, messageContext.getMessage());
        messageContext.setMessage(message);
    }
}
