package net.flectone.pulse.module.message.format.translate.model;

import lombok.Builder;
import lombok.With;
import net.kyori.adventure.text.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    public TranslatedMessage addTranslation(String locale, Component translation) {
        Map<String, Component> newTranslations = new ConcurrentHashMap<>(translations);
        newTranslations.put(locale, translation);
        return withTranslations(newTranslations);
    }

    public boolean hasTranslation(String locale) {
        return translations.containsKey(locale);
    }

    public Component getOriginal() {
        return translations.get(originalLang);
    }
}
