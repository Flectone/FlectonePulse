package net.flectone.pulse.module.message.format.moderation.delete.model;

import lombok.With;
import net.flectone.pulse.module.message.format.translate.model.TranslatedMessage;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@With
public record HistoryMessage(
        UUID uuid,
        Component component,
        @Nullable TranslatedMessage translatedMessage,
        boolean showOriginal
) {

    public HistoryMessage(UUID uuid, Component component) {
        this(uuid, component, null, false);
    }

    public HistoryMessage(UUID uuid, Component component, TranslatedMessage translatedMessage) {
        this(uuid, component, translatedMessage, false);
    }

    /**
     * Get the component to display based on showOriginal flag and player locale.
     */
    public Component getDisplayComponent(String playerLocale) {
        if (translatedMessage == null) {
            return component;
        }

        if (showOriginal) {
            return translatedMessage.getOriginal();
        }

        return translatedMessage.getTranslation(playerLocale);
    }

    /**
     * Check if this message has translations.
     */
    public boolean hasTranslations() {
        return translatedMessage != null;
    }
}
