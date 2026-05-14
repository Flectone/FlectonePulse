package net.flectone.pulse.module.message.format.translate.model;

import lombok.With;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * One entry in TranslateModule's per-receiver chat history.
 *
 * <p>Stores the original formatted component that was first sent to the receiver,
 * plus the {@link TranslatedMessage} carrying translations as they arrive. The
 * {@code showOriginal} flag is flipped by the toggle button to display the
 * original instead of the translation.
 *
 * <p>TranslateModule owns its own copy of this record — it does not share a
 * history mechanism with {@code DeleteModule} on purpose, to keep modules
 * isolated as the plugin author requires.
 */
@With
public record TranslateHistoryMessage(
        UUID uuid,
        Component originalComponent,
        @Nullable TranslatedMessage translatedMessage,
        boolean showOriginal
) {

    public TranslateHistoryMessage(UUID uuid, Component component) {
        this(uuid, component, null, false);
    }

    public TranslateHistoryMessage(UUID uuid, Component component, TranslatedMessage translatedMessage) {
        this(uuid, component, translatedMessage, false);
    }

    /**
     * Pick the component to display for a given receiver locale.
     * - showOriginal=true → always original
     * - receiver locale == source locale → original (no translation done)
     * - else → translation for receiver's locale (falls back to original if not yet ready)
     */
    public Component getDisplayComponent(String receiverLocale) {
        if (translatedMessage == null) {
            return originalComponent;
        }

        if (showOriginal) {
            return originalComponent;
        }

        // No translation needed when receiver's locale matches the source
        if (receiverLocale != null && receiverLocale.equals(translatedMessage.originalLang())) {
            return originalComponent;
        }

        Component translation = translatedMessage.getTranslation(receiverLocale);
        return translation != null ? translation : originalComponent;
    }

    public boolean hasTranslations() {
        return translatedMessage != null;
    }
}
