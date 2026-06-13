package net.flectone.pulse.module.message.format.translate.model;

import lombok.With;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// One entry in TranslateModule's global chat history, shared server-wide (viewers set).
// componentsByLocale holds one Component per receiver locale, because the built line
// differs by locale (e.g. the toggle button is only added when locale != sender's).
// Display reuses these via Component.replaceText to keep original formatting.
@With
public record TranslateHistoryMessage(
        UUID uuid,
        Map<String, Component> componentsByLocale,
        String originalText,
        @Nullable TranslatedMessage translatedMessage,
        Set<UUID> viewers
) {

    public TranslateHistoryMessage {
        if (componentsByLocale == null) componentsByLocale = new ConcurrentHashMap<>();
        if (viewers == null) viewers = ConcurrentHashMap.newKeySet();
    }

    public static TranslateHistoryMessage create(UUID uuid,
                                                 String receiverLocale,
                                                 Component component,
                                                 String originalText,
                                                 @Nullable TranslatedMessage translatedMessage) {
        Map<String, Component> components = new ConcurrentHashMap<>();
        if (receiverLocale != null && component != null) components.put(receiverLocale, component);
        Set<UUID> viewers = ConcurrentHashMap.newKeySet();
        return new TranslateHistoryMessage(uuid, components, originalText, translatedMessage, viewers);
    }

    // Returns the original component unless a usable translation for this locale exists
    // and showOriginal is false, in which case the text literal is swapped for it.
    public Component getDisplayComponent(String receiverLocale, boolean showOriginal) {
        Component base = componentForLocale(receiverLocale);

        if (translatedMessage == null) return base;
        if (showOriginal) return base;
        if (receiverLocale != null && receiverLocale.equals(translatedMessage.originalLang())) return base;

        String translationText = translatedMessage.getTranslation(receiverLocale);
        if (translationText == null || translationText.isEmpty() || translationText.equals(originalText)) {
            return base;
        }
        return base.replaceText(builder -> builder
                .matchLiteral(originalText)
                .replacement(translationText)
        );
    }

    // Component built for this locale, falling back to any stored one if absent.
    public Component componentForLocale(String locale) {
        if (locale != null) {
            Component exact = componentsByLocale.get(locale);
            if (exact != null) return exact;
        }
        return componentsByLocale.values().stream().findFirst().orElse(Component.empty());
    }

    public boolean hasTranslations() {
        return translatedMessage != null;
    }
}
