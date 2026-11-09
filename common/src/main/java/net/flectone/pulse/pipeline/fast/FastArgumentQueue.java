package net.flectone.pulse.pipeline.fast;

import net.kyori.adventure.text.minimessage.internal.parser.Token;
import net.kyori.adventure.text.minimessage.internal.parser.TokenParser;
import net.kyori.adventure.text.minimessage.internal.parser.TokenType;
import net.kyori.adventure.text.minimessage.internal.parser.node.TagPart;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Argument queue for the fast parser. It keeps the raw argument strings and builds Adventure
 * {@link Tag.Argument}s only when a handler asks for them.
 *
 * @author TheFaser
 */
final class FastArgumentQueue implements ArgumentQueue {

    private static final TokenParser.TagProvider NO_PREPROCESS = (_, _, _) -> null;

    private final List<String> arguments;
    private final Function<String, Tag.Argument> argumentFactory;
    private int index;

    FastArgumentQueue(@NonNull List<String> arguments, @NonNull Function<String, Tag.Argument> argumentFactory) {
        this.arguments = arguments;
        this.argumentFactory = argumentFactory;
    }

    static Tag.@NonNull Argument createArgument(@NonNull String value) {
        return new TagPart(value, new Token(0, value.length(), TokenType.TAG_VALUE), NO_PREPROCESS);
    }

    @Override
    public Tag.@NonNull Argument pop() {
        if (!hasNext()) throw new IllegalStateException("Missing argument for this tag!");
        return argument(index++);
    }

    @Override
    public Tag.@NonNull Argument popOr(@NonNull String errorMessage) {
        if (!hasNext()) throw new IllegalStateException(errorMessage);
        return argument(index++);
    }

    @Override
    public Tag.@NonNull Argument popOr(@NonNull Supplier<String> errorMessage) {
        if (!hasNext()) throw new IllegalStateException(errorMessage.get());
        return argument(index++);
    }

    @Override
    public Tag.@Nullable Argument peek() {
        return hasNext() ? argument(index) : null;
    }

    @Override
    public boolean hasNext() {
        return index < arguments.size();
    }

    @Override
    public void reset() {
        index = 0;
    }

    private Tag.@NonNull Argument argument(int position) {
        return argumentFactory.apply(arguments.get(position));
    }

    @Override
    public String toString() {
        return arguments.toString();
    }

}
