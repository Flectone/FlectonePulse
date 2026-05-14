package net.flectone.pulse.module.message.format.translate.model;

import lombok.Builder;
import lombok.With;
import net.kyori.adventure.text.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the original chat text plus per-locale translations.
 *
 * <p>Populated incrementally as async translation results arrive from
 * {@code TranslationCacheService}. The {@link #translations} map is concurrent
 * so callbacks can write while the chat redraw reads.
 */
@With
@Builder(toBuilder = true)
public record TranslatedMessage(
        String originalText,
        String originalLang,
        Map<String, Component> translations
) {

    public TranslatedMessage {
        if (translations == null) translations = new ConcurrentHashMap<>();
    }

    public Component getTranslation(String locale) {
        return translations.getOrDefault(locale, translations.get(originalLang));
    }

    public Component getOriginal() {
        return translations.get(originalLang);
    }

    public boolean hasTranslation(String locale) {
        return translations.containsKey(locale);
    }
}
