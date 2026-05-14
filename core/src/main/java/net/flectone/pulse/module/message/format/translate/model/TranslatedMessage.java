package net.flectone.pulse.module.message.format.translate.model;

import lombok.Builder;
import lombok.With;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the original chat text plus per-locale translation strings (not Components).
 *
 * <p>Translations are kept as plain text — the formatted Component is produced
 * lazily by {@code Component.replaceText} against the stored
 * {@code originalComponent} in {@code TranslateHistoryMessage}, swapping
 * {@code originalText} → translation. This preserves all original formatting
 * (player name, colors, ⇄ button) without rebuilding through the message
 * pipeline.
 *
 * <p>The map is concurrent so async-translation callbacks can write while
 * the chat redraw reads.
 */
@With
@Builder(toBuilder = true)
public record TranslatedMessage(
        String originalText,
        String originalLang,
        Map<String, String> translations
) {

    public TranslatedMessage {
        if (translations == null) translations = new ConcurrentHashMap<>();
    }

    public String getTranslation(String locale) {
        return translations.getOrDefault(locale, translations.get(originalLang));
    }

    public String getOriginal() {
        return translations.get(originalLang);
    }

    public boolean hasTranslation(String locale) {
        return translations.containsKey(locale);
    }
}
