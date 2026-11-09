package net.flectone.pulse.util.tag;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Collects the tag names a MiniMessage string uses, so a module can skip building a resolver
 * for a tag the message never contains.
 *
 * <p>The scan is intentionally permissive: a false positive only costs one unused resolver,
 * while a false negative would leave a real tag unresolved.</p>
 *
 * @author TheFaser
 */
public final class TagPresence {

    private TagPresence() {
    }

    /**
     * Collects every tag name written in the text.
     *
     * @param message the raw text
     * @return the lowercased tag names, empty when the text has no tags
     */
    public static @NonNull Set<String> scan(@NonNull String message) {
        int length = message.length();
        Set<String> names = null;

        for (int index = 0; index < length; index++) {
            if (message.charAt(index) != '<') continue;

            int nameStart = index + 1;
            int tagEnd = -1;
            char quote = 0;

            for (int cursor = nameStart; cursor < length; cursor++) {
                char character = message.charAt(cursor);

                if (quote != 0) {
                    if (character == quote) quote = 0;
                    continue;
                }

                if ((character == '\'' || character == '"')
                        && message.indexOf(character, cursor + 1) != -1) {
                    quote = character;
                } else if (character == '<') {
                    // the MiniMessage tokenizer restarts a tag at a nested '<'
                    nameStart = cursor + 1;
                } else if (character == '>') {
                    tagEnd = cursor;
                    break;
                }
            }

            if (tagEnd < 0) break;

            String name = readName(message, nameStart, tagEnd);
            if (name != null) {
                if (names == null) names = new HashSet<>(4);
                names.add(name);
            }

            index = tagEnd;
        }

        return names == null ? Set.of() : names;
    }

    /**
     * Checks the text for one tag name.
     *
     * @param message the raw text
     * @param name the lowercased tag name
     * @return true if the text uses the tag
     */
    public static boolean contains(@NonNull String message, @NonNull String name) {
        return scan(message).contains(name);
    }

    private static @Nullable String readName(@NonNull String message, int start, int end) {
        // closing tags start with '/', reverting tags with '!'
        while (start < end) {
            char character = message.charAt(start);
            if (character != '/' && character != '!') break;
            start++;
        }

        int nameStart = start;
        while (start < end) {
            char character = message.charAt(start);
            if (character == ':' || character == '/' || character == ' '
                    || character == '\t' || character == '\'' || character == '"') break;
            start++;
        }

        if (nameStart == start) return null;

        return message.substring(nameStart, start).toLowerCase(Locale.ROOT);
    }

}
