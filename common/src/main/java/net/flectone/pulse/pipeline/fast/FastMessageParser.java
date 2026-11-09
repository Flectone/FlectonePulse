package net.flectone.pulse.pipeline.fast;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

@Singleton
public class FastMessageParser {

    private static final String CONTEXT_TAG = "__flectonepulse_context__";

    private final MiniMessage miniMessage;
    private final Context context;
    private final ThreadLocal<TagResolver> currentResolver = new ThreadLocal<>();
    private final Cache<String, Tag.Argument> argumentCache = Caffeine.newBuilder()
            .maximumSize(4096)
            .expireAfterAccess(Duration.ofMinutes(10))
            .build();

    @Inject
    public FastMessageParser(@NonNull MiniMessage miniMessage) {
        this.miniMessage = miniMessage;
        this.context = captureContext();
    }

    public @Nullable Component deserialize(@NonNull String message, @NonNull TagResolver resolver) {
        TagResolver previous = currentResolver.get();
        currentResolver.set(resolver);

        try {
            Component component = new FastParser(message, resolver, miniMessage, context, this::argument).parse();
            return component == null ? null : compact(component);
        } catch (Exception _) {
            // the regular parser will either render the message or log the real error
            return null;
        } finally {
            if (previous == null) {
                currentResolver.remove();
            } else {
                currentResolver.set(previous);
            }
        }
    }

    private Tag.@NonNull Argument argument(@NonNull String value) {
        if (value.indexOf('<') >= 0) {
            return FastArgumentQueue.createArgument(value);
        }

        return argumentCache.get(value, FastArgumentQueue::createArgument);
    }

    static @NonNull Component compact(@NonNull Component component) {
        if (component instanceof TextComponent textComponent && textComponent.children().isEmpty()) {
            return component;
        }

        return component.compact();
    }

    private @NonNull Context captureContext() {
        Context[] captured = new Context[1];

        TagResolver delegated = new TagResolver() {
            @Override
            public @Nullable Tag resolve(@NonNull String name,
                                         @NonNull ArgumentQueue arguments,
                                         @NonNull Context ctx) {
                TagResolver current = currentResolver.get();
                return current == null ? null : current.resolve(name, arguments, ctx);
            }

            @Override
            public boolean has(@NonNull String name) {
                TagResolver current = currentResolver.get();
                return current != null && current.has(name);
            }
        };

        TagResolver capture = TagResolver.resolver(CONTEXT_TAG, (_, ctx) -> {
            captured[0] = ctx;
            return Tag.selfClosingInserting(Component.empty());
        });

        miniMessage.deserialize("<" + CONTEXT_TAG + ">", TagResolver.resolver(delegated, capture));

        if (captured[0] == null) {
            throw new IllegalStateException("Failed to capture an Adventure Context");
        }

        return captured[0];
    }

}
