package net.flectone.pulse.module.message.format.translate.model;

import lombok.With;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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
        return replaceMessageText(base, originalText, translationText);
    }

    // Swaps the original message text for its translation inside an already-built component.
    //
    // Why not Component.replaceText(matchLiteral(...)): Adventure's matchLiteral only matches
    // a literal that lies WITHIN the content of a single text node (a contiguous "run" of the
    // same style/events). Format modules (mention, fixation, etc.) frequently split the player
    // message into several sibling nodes with DIFFERENT styles/hover/click events
    // (e.g. "✉ A → B » " + "привет"[hover] + " как " + "дела"). For chat the message usually
    // stays one node, so matchLiteral works there; for private messages it gets split, so
    // matchLiteral silently fails to match across the node boundaries and the original text
    // leaks through untranslated.
    //
    // This walks the component tree depth-first, finds the first occurrence of `original` in the
    // flattened plain text (the same offset Adventure's first-match would target), and rewrites
    // exactly that span — even when it spans nodes with differing styles. The replacement text is
    // emitted into the node where the match starts (inheriting its style); the remaining matched
    // characters in later nodes are dropped. Surrounding format and any text after the message
    // (toggle button, etc.) are preserved verbatim. No-op if the literal isn't present.
    //
    // CRITICAL: the plain-text cursor must advance for EVERY node that contributes plain text,
    // not only TextComponents. Player-head nodes and other format pieces can be Translatable /
    // non-text components (e.g. "[SolVerNA head]"); they still emit characters into the flattened
    // plain text. The previous version only advanced the cursor on TextComponents, so any such
    // node sitting before the message shifted every later offset and the match landed in the wrong
    // place — that produced the garbled chat output (original not cut) and the silent ЛС miss.
    public static Component replaceMessageText(Component root, String original, String replacement) {
        if (root == null) return Component.empty();
        if (original == null || original.isEmpty() || replacement == null) return root;

        String plain = PlainTextComponentSerializer.plainText().serialize(root);
        int start = plain.indexOf(original);
        if (start < 0) return root;

        return walk(root, new ReplaceState(start, original.length(), replacement));
    }

    private static Component walk(Component node, ReplaceState state) {
        // result == node with its children stripped, i.e. only this node's own value.
        Component result = node.children(List.of());
        if (node instanceof TextComponent textComponent && !textComponent.content().isEmpty()) {
            result = ((TextComponent) result).content(state.transform(textComponent.content()));
        } else {
            // Non-text node (translatable head, score, etc.) or empty-content text node: it still
            // contributes characters to the flattened plain text, so advance the cursor by its own
            // plain-text length (serialized without children) to keep later offsets aligned with
            // PlainTextComponentSerializer. We never edit such nodes — the message lives in
            // TextComponents — but their length must be accounted for.
            String own = PlainTextComponentSerializer.plainText().serialize(result);
            if (!own.isEmpty()) state.advance(own.length());
        }
        List<Component> newChildren = new ArrayList<>(node.children().size());
        for (Component child : node.children()) {
            newChildren.add(walk(child, state));
        }
        return result.children(newChildren);
    }

    // Mutable cursor shared across the depth-first walk: tracks the absolute plain-text offset
    // reached so far and how much of the literal has been consumed.
    private static final class ReplaceState {
        private final int matchStart;
        private final int matchLength;
        private final String replacement;
        private int position = 0;
        private int matchedChars = 0;
        private boolean emittedReplacement = false;
        private boolean finished = false;

        private ReplaceState(int matchStart, int matchLength, String replacement) {
            this.matchStart = matchStart;
            this.matchLength = matchLength;
            this.replacement = replacement;
        }

        // Advance the plain-text cursor past characters emitted by a node we don't edit
        // (non-text nodes, e.g. translatable player heads), keeping offsets in sync.
        private void advance(int chars) {
            position += chars;
        }

        private String transform(String content) {
            if (finished) {
                position += content.length();
                return content;
            }
            StringBuilder out = new StringBuilder(content.length());
            for (int i = 0; i < content.length(); i++) {
                int abs = position + i;
                if (!finished && matchedChars < matchLength && abs >= matchStart) {
                    if (!emittedReplacement) {
                        out.append(replacement);
                        emittedReplacement = true;
                    }
                    matchedChars++;
                    if (matchedChars == matchLength) finished = true;
                    continue; // drop the original matched character
                }
                out.append(content.charAt(i));
            }
            position += content.length();
            return out.toString();
        }
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
