package net.flectone.pulse.util.tag;

import net.flectone.pulse.logging.FLogger;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Tag resolver for a set of tag names that keeps the tags it builds, so a tag written several times
 * in one message is built only once.
 *
 * @author TheFaser
 * @since 1.13.0
 * @see TagResolver
 */
// wait for https://github.com/PaperMC/adventure/issues/1424
public final class CachingTagResolver implements TagResolver {

    private final Set<String> names;
    private final BiFunction<ArgumentQueue, Context, Tag> handler;
    private final FLogger fLogger;

    private String cachedKey;
    private Tag cachedTag;
    private Map<String, Tag> cachedTags; // lazy map

    /**
     * Creates a resolver serving the given tag names.
     *
     * @param names the tag names this resolver answers to
     * @param handler builds the tag from the arguments it is written with
     * @param fLogger logs a handler that failed to build its tag
     */
    public CachingTagResolver(@NonNull Set<String> names,
                              @NonNull BiFunction<ArgumentQueue, Context, Tag> handler,
                              @NonNull FLogger fLogger) {
        this.names = names;
        this.handler = handler;
        this.fLogger = fLogger;
    }

    @Override
    public @Nullable Tag resolve(@NonNull String name, @NonNull ArgumentQueue arguments, @NonNull Context context) throws ParsingException {
        if (!names.contains(name)) return null;

        // build cache key from tag name + all arguments
        // a tag without arguments is the common case and its name alone tells it apart
        String key = arguments.hasNext() ? name + ":" + arguments : name;

        // multiple unique keys seen, use map
        if (cachedTags != null) {
            return cachedTags.computeIfAbsent(key, _ -> handler.apply(arguments, context));
        }

        // first call, store in fields to avoid map allocation
        if (cachedKey == null) {
            cachedKey = key;
            cachedTag = handler.apply(arguments, context);
            return cachedTag;
        }

        // same key as before, return cached result
        if (cachedKey.equals(key)) return cachedTag;

        // second unique key seen, upgrade to map
        cachedTags = new HashMap<>();
        cachedTags.put(cachedKey, cachedTag);

        try {
            // create tag
            Tag tag = handler.apply(arguments, context);

            // save to cache
            cachedTags.put(key, tag);

            // return tag
            return tag;
        } catch (ParsingException e) {
            fLogger.warning(e);
        }

        return null;
    }

    @Override
    public boolean has(final @NonNull String name) {
        return names.contains(name);
    }

}
