package net.flectone.pulse.module.message.format.translate.model;

import lombok.With;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * One entry in TranslateModule's per-receiver chat history.
 *
 * <p>Stores the original formatted component that was first sent to the receiver
 * plus the raw player text and the {@link TranslatedMessage} carrying translation
 * strings as they arrive. The {@code showOriginal} flag is flipped by the toggle
 * button to display the original instead of the translation.
 *
 * <p>Formatting is preserved on display: we never rebuild the message through
 * the pipeline. Instead {@link Component#replaceText} swaps the original text
 * literal with the translation inside the existing component tree — colors,
 * prefix, nickname, ⇄ button all stay intact.
 *
 * <p>TranslateModule owns its own copy of this record — it does not share a
 * history mechanism with {@code DeleteModule} on purpose, to keep modules
 * isolated as the plugin author requires.
 */
@With
public record TranslateHistoryMessage(
        UUID uuid,
        Component originalComponent,
        String originalText,
        @Nullable TranslatedMessage translatedMessage,
        boolean showOriginal
) {

    public TranslateHistoryMessage(UUID uuid, Component component) {
        this(uuid, component, "", null, false);
    }

    public TranslateHistoryMessage(UUID uuid, Component component, String originalText, TranslatedMessage translatedMessage) {
        this(uuid, component, originalText, translatedMessage, false);
    }

    /**
     * Pick the component to display for a given receiver locale.
     * - showOriginal=true → always original
     * - receiver locale == source locale → original (no translation done)
     * - translation not yet arrived for this locale → original (fallback)
     * - else → original component with text literal replaced by translation
     *   (preserves all surrounding formatting via {@link Component#replaceText})
     */
    public Component getDisplayComponent(String receiverLocale) {
        if (translatedMessage == null) {
            return originalComponent;
        }

        if (showOriginal) {
            return originalComponent;
        }

        if (receiverLocale != null && receiverLocale.equals(translatedMessage.originalLang())) {
            return originalComponent;
        }

        String translationText = translatedMessage.getTranslation(receiverLocale);
        if (translationText == null || translationText.isEmpty() || translationText.equals(originalText)) {
            return originalComponent;
        }

        return originalComponent.replaceText(builder -> builder
                .matchLiteral(originalText)
                .replacement(translationText)
        );
    }

    public boolean hasTranslations() {
        return translatedMessage != null;
    }
}
