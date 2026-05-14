package net.flectone.pulse.module.message.format.translate.model;

import lombok.With;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One entry in TranslateModule's <b>global</b> chat history.
 *
 * <p>The history is shared across the whole server: every chat event is stored
 * once, and the set of players who saw it lives in {@code viewers}. When a
 * player needs a chat redraw, we filter the global list by viewer membership
 * and render each remaining entry using that player's locale plus their own
 * toggle state (kept separately in TranslateModule).
 *
 * <p>Memory: 100 players seeing the same message = 1 entry with a 100-sized
 * viewer set, instead of 100 duplicated entries.
 *
 * <p>{@code showOriginal} flag is <b>not</b> here — it's per-player state, kept
 * in {@code TranslateModule.playerOriginalToggles}.
 *
 * <p>Formatting is preserved on display: we never rebuild the message through
 * the pipeline. {@link Component#replaceText} swaps the original text literal
 * with the translation inside the existing component tree — colors, prefix,
 * nickname, ⇄ button all stay intact.
 */
@With
public record TranslateHistoryMessage(
        UUID uuid,
        Component originalComponent,
        String originalText,
        @Nullable TranslatedMessage translatedMessage,
        Set<UUID> viewers
) {

    public TranslateHistoryMessage {
        if (viewers == null) viewers = ConcurrentHashMap.newKeySet();
    }

    public TranslateHistoryMessage(UUID uuid, Component component) {
        this(uuid, component, "", null, ConcurrentHashMap.newKeySet());
    }

    public TranslateHistoryMessage(UUID uuid, Component component, String originalText, TranslatedMessage translatedMessage) {
        this(uuid, component, originalText, translatedMessage, ConcurrentHashMap.newKeySet());
    }

    /**
     * Pick the component to display for a given receiver locale and per-player toggle state.
     * - showOriginal=true → original
     * - no translatedMessage → original
     * - receiver locale == source locale → original (no translation done)
     * - translation not yet arrived for this locale → original (fallback)
     * - else → original component with text literal replaced by translation
     */
    public Component getDisplayComponent(String receiverLocale, boolean showOriginal) {
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
