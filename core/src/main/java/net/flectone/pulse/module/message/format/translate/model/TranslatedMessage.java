package net.flectone.pulse.module.message.format.translate.model;

import lombok.Builder;
import lombok.With;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Original chat text plus per-locale translation strings (plain text, not Components —
// the Component is produced lazily via replaceText in TranslateHistoryMessage).
// The map is concurrent so async callbacks can write while the redraw reads.
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
