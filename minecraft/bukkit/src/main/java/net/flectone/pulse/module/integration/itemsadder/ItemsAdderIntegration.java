package net.flectone.pulse.module.integration.itemsadder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.message.format.convertor.LegacyColorConvertor;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permissible;
import org.jspecify.annotations.Nullable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ItemsAdderIntegration implements FIntegration, PulseListener {

    private final LegacyColorConvertor legacyColorConvertor;
    @Getter private final FLogger fLogger;

    @Getter private boolean hooked;

    @Override
    public String getIntegrationName() {
        return "ItemsAdder";
    }

    @Override
    public void hook() {
        hooked = true;
        logHook();
    }

    @Override
    public void unhook() {
        hooked = false;
        logUnhook();
    }

    @Pulse(priority = Event.Priority.LOW)
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        if (!isHooked()) return event;

        Permissible permissible = Bukkit.getPlayer(messageContext.sender().uuid());
        if (StringUtils.isNotEmpty(messageContext.userMessage())) {
            messageContext = messageContext.withUserMessage(formatFontImages(permissible, messageContext.userMessage()));
        }

        return event.withContext(messageContext.withMessage(formatFontImages(permissible, messageContext.message())));
    }

    private String formatFontImages(@Nullable Permissible permissible, String message) {
        // ItemsAdder returns a string with legacy colors, so we need to format them
        return legacyColorConvertor.convert(permissible != null
                ? FontImageWrapper.replaceFontImages(permissible, message)
                : FontImageWrapper.replaceFontImages(message)
        );
    }
}
