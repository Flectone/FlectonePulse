package net.flectone.pulse.util.tag;

import org.jspecify.annotations.NonNull;

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
            while (nameStart < length) {
                char character = message.charAt(nameStart);
                if (character != '/' && character != '!') break;

                nameStart++;
            }

            int nameEnd = nameStart;
            while (nameEnd < length) {
                char character = message.charAt(nameEnd);
                if (character == ':' || character == '/'
                        || character == '>' || character == '\''
                        || character == '"' || Character.isWhitespace(character)) break;

                nameEnd++;
            }

            if (nameStart == nameEnd) continue;

            if (names == null) {
                names = new HashSet<>(4);
            }

            names.add(message.substring(nameStart, nameEnd).toLowerCase(Locale.ROOT));
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

}
