package net.flectone.pulse.util.tag;

import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

/**
 * Stack of tag resolvers that is merged into a single resolver only when a tag is first looked up.
 * {@link TagResolver#resolver(TagResolver...)} merges every resolver it is given into a new map and
 * a new array, so merging on each addition copies the whole stack again and again, which is
 * quadratic in the number of formatting modules a message goes through
 *
 * @author TheFaser
 * @since 1.13.0
 * @see TagResolver
 */
public final class LazyTagResolver implements TagResolver {

    private final TagResolver parent;
    private final TagResolver[] resolvers;
    private final int size;

    private @Nullable TagResolver merged;

    private LazyTagResolver(@NonNull TagResolver parent, TagResolver @NonNull [] resolvers) {
        this.parent = parent;
        this.resolvers = resolvers;
        this.size = (parent instanceof LazyTagResolver lazy ? lazy.size : 1) + resolvers.length;
    }

    /**
     * Stacks a resolver on top of the current ones
     *
     * @param current the resolvers stacked so far
     * @param resolver the resolver to add
     * @return the stacked resolver, or an empty resolver if there is nothing to stack
     */
    public static @NonNull TagResolver append(@Nullable TagResolver current, @Nullable TagResolver resolver) {
        if (resolver == null) return current == null ? TagResolver.empty() : current;
        if (current == null) return resolver;

        return new LazyTagResolver(current, new TagResolver[]{resolver});
    }

    /**
     * Stacks several resolvers on top of the current ones
     *
     * @param current the resolvers stacked so far
     * @param resolvers the resolvers to add
     * @return the stacked resolver, or an empty resolver if there is nothing to stack
     */
    public static @NonNull TagResolver appendAll(@Nullable TagResolver current, TagResolver @NonNull ... resolvers) {
        if (resolvers.length == 0) return current == null ? TagResolver.empty() : current;
        if (current == null) {
            return resolvers.length == 1
                    ? resolvers[0]
                    : new LazyTagResolver(resolvers[0], Arrays.copyOfRange(resolvers, 1, resolvers.length));
        }

        return new LazyTagResolver(current, resolvers.clone());
    }

    @Override
    public @Nullable Tag resolve(@NonNull String name, @NonNull ArgumentQueue arguments, @NonNull Context ctx) throws ParsingException {
        return merged().resolve(name, arguments, ctx);
    }

    @Override
    public boolean has(@NonNull String name) {
        return merged().has(name);
    }

    /**
     * Merges the whole stack into a single resolver on first use and keeps it for the calls after that.
     *
     * @return the merged resolver
     */
    public @NonNull TagResolver merged() {
        if (merged == null) {
            TagResolver[] parts = new TagResolver[size];

            int index = size;
            TagResolver current = this;

            while (current instanceof LazyTagResolver lazy) {
                index -= lazy.resolvers.length;
                System.arraycopy(lazy.resolvers, 0, parts, index, lazy.resolvers.length);
                current = lazy.parent;
            }

            parts[index - 1] = current;

            this.merged = TagResolver.resolver(parts);
        }

        return merged;
    }

}
