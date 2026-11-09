package net.flectone.pulse.pipeline;

import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.util.tag.LazyTagResolver;
import net.flectone.pulse.util.tag.TagPresence;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Mutable context used only while the formatting listeners add their tags.
 *
 */
final class FormattingContext implements MessageContext {

    private final List<TagResolver> resolvers = new ArrayList<>(4);

    private MessageContext base;
    private @Nullable TagResolver resolved;

    private @Nullable String scannedMessage;
    private @NonNull Set<String> presentTags = Set.of();

    FormattingContext(@NonNull MessageContext base) {
        this.base = base;
    }

    @Override
    public @NonNull MessageContext base() {
        return base;
    }

    @Override
    public MessageContext withBase(@NonNull MessageContext base) {
        this.base = base;
        this.resolved = null;
        return this;
    }

    @Override
    public MessageContext withTagResolver(@NonNull TagResolver tagResolver) {
        this.resolvers.add(tagResolver);
        this.resolved = null;
        return this;
    }

    @Override
    public MessageContext addTagResolver(@Nullable TagResolver tagResolver) {
        if (tagResolver == null) return this;

        return withTagResolver(tagResolver);
    }

    @Override
    public MessageContext addTagResolvers(TagResolver @NonNull ... tagResolvers) {
        if (tagResolvers == null || tagResolvers.length == 0) return this;

        for (TagResolver tagResolver : tagResolvers) {
            if (tagResolver != null) {
                this.resolvers.add(tagResolver);
            }
        }

        this.resolved = null;
        return this;
    }

    @Override
    public boolean hasTag(@NonNull String name) {
        String message = base.message();
        if (!message.equals(scannedMessage)) {
            scannedMessage = message;
            presentTags = TagPresence.scan(message);
        }

        return presentTags.contains(name);
    }

    @Override
    public @NonNull TagResolver tagResolver() {
        TagResolver tagResolver = this.resolved;
        if (tagResolver == null) {
            if (resolvers.isEmpty()) {
                tagResolver = base.tagResolver();
            } else {
                TagResolver[] all = new TagResolver[resolvers.size() + 1];
                all[0] = base.tagResolver();
                for (int index = 0; index < resolvers.size(); index++) {
                    all[index + 1] = resolvers.get(index);
                }

                tagResolver = LazyTagResolver.of(all);
            }

            this.resolved = tagResolver;
        }

        return tagResolver;
    }

}
