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
 * Flat stack of tag resolvers.
 *
 * <p>{@link TagResolver#resolver(TagResolver...)} merges every resolver into a map on each
 * addition. This class keeps one array instead and walks it from the newest resolver to the
 * oldest one, so adding a resolver is one array copy and resolving a tag does not build a map.</p>
 *
 * @author TheFaser
 * @since 1.13.0
 * @see TagResolver
 */
public final class LazyTagResolver implements TagResolver {

    private final TagResolver[] resolvers;

    private LazyTagResolver(TagResolver @NonNull [] resolvers) {
        this.resolvers = resolvers;
    }

    /**
     * Stacks a resolver on top of the current ones.
     *
     * @param current the resolvers stacked so far
     * @param resolver the resolver to add
     * @return the stacked resolver, or an empty resolver if there is nothing to stack
     */
    public static @NonNull TagResolver append(@Nullable TagResolver current, @Nullable TagResolver resolver) {
        if (resolver == null) return current == null ? TagResolver.empty() : current;

        return appendAll(current, resolver);
    }

    /**
     * Stacks several resolvers on top of the current ones.
     *
     * @param current the resolvers stacked so far
     * @param added the resolvers to add
     * @return the stacked resolver, or an empty resolver if there is nothing to stack
     */
    public static @NonNull TagResolver appendAll(@Nullable TagResolver current, TagResolver @NonNull ... added) {
        if (added.length == 0) return current == null ? TagResolver.empty() : current;

        if (current == null) {
            return added.length == 1 ? added[0] : new LazyTagResolver(Arrays.copyOfRange(added, 0, added.length));
        }

        if (current instanceof LazyTagResolver lazy) {
            TagResolver[] merged = Arrays.copyOfRange(lazy.resolvers, 0, lazy.resolvers.length + added.length);
            System.arraycopy(added, 0, merged, lazy.resolvers.length, added.length);
            return new LazyTagResolver(merged);
        }

        TagResolver[] merged = new TagResolver[added.length + 1];
        merged[0] = current;
        System.arraycopy(added, 0, merged, 1, added.length);
        return new LazyTagResolver(merged);
    }

    /**
     * Wraps an array the caller just built and will not mutate.
     *
     * @param resolvers the owned resolvers
     * @return the resolver
     */
    public static @NonNull TagResolver of(TagResolver @NonNull [] resolvers) {
        if (resolvers.length == 0) return TagResolver.empty();
        if (resolvers.length == 1) return resolvers[0];

        return new LazyTagResolver(resolvers);
    }

    @Override
    public @Nullable Tag resolve(@NonNull String name, @NonNull ArgumentQueue arguments, @NonNull Context ctx) throws ParsingException {
        ParsingException thrown = null;

        // the newest resolver wins, so walk the flat stack backwards
        for (int index = resolvers.length - 1; index >= 0; index--) {
            TagResolver resolver = resolvers[index];
            if (!resolver.has(name)) continue;

            try {
                Tag tag = resolver.resolve(name, arguments, ctx);
                if (tag != null) return tag;
            } catch (ParsingException e) {
                arguments.reset();
                if (thrown == null) {
                    thrown = e;
                } else {
                    thrown.addSuppressed(e);
                }
            } catch (Exception e) {
                arguments.reset();
                ParsingException error = new TagParsingException("Exception thrown while parsing <" + name + ">", e);

                if (thrown == null) {
                    thrown = error;
                } else {
                    thrown.addSuppressed(error);
                }
            }
        }

        if (thrown != null) {
            throw thrown;
        }

        return null;
    }

    @Override
    public boolean has(@NonNull String name) {
        for (int index = resolvers.length - 1; index >= 0; index--) {
            if (resolvers[index].has(name)) return true;
        }

        return false;
    }

}
