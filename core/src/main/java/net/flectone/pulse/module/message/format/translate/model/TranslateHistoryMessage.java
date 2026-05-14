package net.flectone.pulse.module.message.format.translate.model;

import lombok.With;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One entry in TranslateModule's <b>global</b> chat history.
 *
 * <p>History is shared server-wide: every chat event is stored once, with the
 * set of viewers per entry. Per-player state (showOriginal toggle) lives in
 * a separate map in TranslateModule — not on this record.
 *
 * <p>Per-locale Components: the visible chat line built by the pipeline differs
 * across receiver locales — e.g. the ⇄ toggle button is only inserted for
 * receivers whose locale differs from the sender's. So we keep
 * {@code componentsByLocale}: one Component per receiver locale seen during
 * the SendEvent fan-out. Memory remains bounded — at most one Component per
 * unique locale on the server, not one per receiver.
 *
 * <p>Formatting is preserved on display: we never rebuild the message through
 * the pipeline. {@link Component#replaceText} swaps the original text literal
 * with the translation inside the existing component tree.
 */
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

    /**
     * Pick the component to display for a given receiver locale and per-player toggle state.
     * - showOriginal=true → original component as it was built for this locale
     * - no translatedMessage → original component
     * - receiver locale == source locale → original component (no translation done)
     * - translation not yet arrived for this locale → original component (fallback)
     * - else → original component with text literal replaced by translation
     */
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

    /**
     * Pick the Component built for {@code locale}. Falls back to any stored
     * Component (first wins) if exact locale isn't recorded — shouldn't happen
     * in practice but keeps us safe.
     */
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
