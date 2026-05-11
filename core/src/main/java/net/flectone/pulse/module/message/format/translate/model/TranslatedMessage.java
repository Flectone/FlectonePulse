package net.flectone.pulse.module.message.format.translate.model;

import lombok.Builder;
import lombok.With;
import net.kyori.adventure.text.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a message with translations to multiple languages.
 * Stores the original text and all translations to avoid repeated API calls.
 */
@With
@Builder(toBuilder = true)
public record TranslatedMessage(
        UUID uuid,
        String originalText,
        String originalLang,
        Map<String, Component> translations
) {

    public TranslatedMessage {
        if (uuid == null) uuid = UUID.randomUUID();
        if (translations == null) translations = new ConcurrentHashMap<>();
    }

    /**
     * Get translation for specific locale.
     * Returns original if translation not found.
     */
    public Component getTranslation(String locale) {
        return translations.getOrDefault(locale, translations.get(originalLang));
    }

    /**
     * Add translation for specific locale.
     */
    public TranslatedMessage addTranslation(String locale, Component translation) {
        Map<String, Component> newTranslations = new ConcurrentHashMap<>(translations);
        newTranslations.put(locale, translation);
        return withTranslations(newTranslations);
    }

    /**
     * Check if translation exists for locale.
     */
    public boolean hasTranslation(String locale) {
        return translations.containsKey(locale);
    }

    /**
     * Get original message component.
     */
    public Component getOriginal() {
        return translations.get(originalLang);
    }
}
