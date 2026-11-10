package net.flectone.pulse.pipeline.fast;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Inserting;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.ParserDirective;
import net.kyori.adventure.text.minimessage.tag.PreProcess;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

// https://github.com/MrLarkyy/QuickMiniMessage
final class FastParser {

    private static final Cache<String, TextColor> COLOR_CACHE = Caffeine.newBuilder()
            .maximumSize(512)
            .build();

    private final String input;
    private final TagResolver resolver;
    private final MiniMessage miniMessage;
    private final Context context;
    private final Function<String, Tag.Argument> argumentFactory;

    private final Deque<Frame> frames = new ArrayDeque<>(4);
    private final StringBuilder text = new StringBuilder(64);
    private boolean unsupported;

    FastParser(@NonNull String input,
               @NonNull TagResolver resolver,
               @NonNull MiniMessage miniMessage,
               @NonNull Context context,
               @NonNull Function<String, Tag.Argument> argumentFactory) {
        this.input = input;
        this.resolver = resolver;
        this.miniMessage = miniMessage;
        this.context = context;
        this.argumentFactory = argumentFactory;
    }

    @Nullable Component parse() {
        if (input.indexOf('<') < 0 && input.indexOf('\\') < 0) {
            return Component.text(input);
        }

        frames.push(new Frame(null, List.of(), Component.empty()));
        parseSegment(input, 0, input.length());

        flushText();
        while (frames.size() > 1) {
            closeTop();
        }

        if (unsupported) {
            return null;
        }

        return combine(frames.peek().children);
    }

    private void parseSegment(@NonNull String source, int start, int end) {
        int index = start;
        int nextSpecial = -1;

        while (index < end) {
            if (unsupported) return;

            if (nextSpecial < index) {
                nextSpecial = source.indexOf('<', index);
                int escape = source.indexOf('\\', index);
                if (nextSpecial == -1 || (escape != -1 && escape < nextSpecial)) {
                    nextSpecial = escape;
                }
            }

            if (nextSpecial == -1 || nextSpecial >= end) {
                text.append(source, index, end);
                return;
            }

            if (nextSpecial > index) {
                text.append(source, index, nextSpecial);
                index = nextSpecial;
            }

            char current = source.charAt(index);

            if (current == '\\') {
                if (index + 1 < end) {
                    char next = source.charAt(index + 1);
                    if (next == '<' || next == '\\') {
                        text.append(next);
                        index += 2;
                        nextSpecial = -1;
                        continue;
                    }
                }

                text.append('\\');
                index++;
                nextSpecial = -1;
                continue;
            }

            int tagEnd = findTagEnd(source, index, end);
            if (tagEnd < 0) {
                text.append('<');
                index++;
                nextSpecial = -1;
                continue;
            }

            TagInfo tag = readTag(source, index, tagEnd);
            if (tag == null) {
                text.append(source, index, tagEnd + 1);
                index = tagEnd + 1;
                nextSpecial = -1;
                continue;
            }

            handleTag(tag);
            index = tagEnd + 1;
            nextSpecial = -1;
        }
    }

    private int findTagEnd(@NonNull String source, int start, int end) {
        int index = start + 1;
        char quote = 0;
        boolean escaped = false;

        while (index < end) {
            char current = source.charAt(index);

            if (escaped) {
                escaped = false;
                index++;
                continue;
            }

            if (current == '\\') {
                escaped = true;
                index++;
                continue;
            }

            if (quote != 0) {
                if (current == quote) quote = 0;
                index++;
                continue;
            }

            if (current == '\'' || current == '"') {
                quote = current;
                index++;
                continue;
            }

            if (current == '>') return index;
            if (current == '<') return -1;

            index++;
        }

        return -1;
    }

    private @Nullable TagInfo readTag(@NonNull String source, int start, int end) {
        int bodyStart = start + 1;
        boolean closing = false;

        if (bodyStart < end && source.charAt(bodyStart) == '/') {
            closing = true;
            bodyStart++;
        }

        int bodyEnd = end;
        boolean selfClosing = false;
        if (!closing && bodyEnd > bodyStart && source.charAt(bodyEnd - 1) == '/') {
            selfClosing = true;
            bodyEnd--;
        }

        if (bodyStart >= bodyEnd) return null;

        boolean hasArguments = false;
        for (int index = bodyStart; index < bodyEnd; index++) {
            if (source.charAt(index) == ':') {
                hasArguments = true;
                break;
            }
        }

        if (!hasArguments) {
            return new TagInfo(source, start, end + 1, asciiLowercase(source.substring(bodyStart, bodyEnd)), List.of(), closing, selfClosing);
        }

        List<String> parts = splitArgs(source, bodyStart, bodyEnd);
        if (parts == null || parts.isEmpty()) return null;

        String name = parts.getFirst();
        if (name.isEmpty()) return null;

        List<String> arguments = parts.size() == 1
                ? List.of()
                : List.copyOf(parts.subList(1, parts.size()));

        return new TagInfo(source, start, end + 1, asciiLowercase(name), arguments, closing, selfClosing);
    }

    private @Nullable List<String> splitArgs(@NonNull String source, int start, int end) {
        boolean special = false;
        for (int i = start; i < end; i++) {
            char value = source.charAt(i);
            if (value == ':' || value == '\'' || value == '"' || value == '\\') {
                special = true;
                break;
            }
        }

        if (!special) {
            return List.of(source.substring(start, end));
        }

        List<String> parts = new ArrayList<>(4);

        int index = start;
        int segmentStart = start;
        boolean segmentSpecial = false;
        char quote = 0;
        boolean escaped = false;

        while (index < end) {
            char value = source.charAt(index);

            if (escaped) {
                escaped = false;
                segmentSpecial = true;
                index++;
                continue;
            }

            if (value == '\\') {
                escaped = true;
                segmentSpecial = true;
                index++;
                continue;
            }

            if (quote != 0) {
                if (value == quote) quote = 0;
                segmentSpecial = true;
                index++;
                continue;
            }

            if (value == '\'' || value == '"') {
                quote = value;
                segmentSpecial = true;
                index++;
                continue;
            }

            if (value == ':') {
                // keep https://, run_command:... intact
                if (index + 2 < end && source.charAt(index + 1) == '/' && source.charAt(index + 2) == '/') {
                    index++;
                    continue;
                }

                parts.add(readPart(source, segmentStart, index, segmentSpecial));
                segmentStart = index + 1;
                segmentSpecial = false;
                index++;
                continue;
            }

            index++;
        }

        parts.add(readPart(source, segmentStart, end, segmentSpecial));
        return parts;
    }

    private @NonNull String readPart(@NonNull String source, int start, int end, boolean special) {
        if (!special) {
            return source.substring(start, end);
        }

        String quoted = simpleQuoted(source, start, end);
        if (quoted != null) return quoted;

        StringBuilder current = new StringBuilder(end - start);
        char quote = 0;
        boolean escaped = false;

        for (int index = start; index < end; index++) {
            char value = source.charAt(index);

            if (escaped) {
                current.append(value);
                escaped = false;
                continue;
            }

            if (value == '\\') {
                escaped = true;
                continue;
            }

            if (quote != 0) {
                if (value == quote) {
                    quote = 0;
                } else {
                    current.append(value);
                }
                continue;
            }

            if (value == '\'' || value == '"') {
                quote = value;
                continue;
            }

            current.append(value);
        }

        return current.toString();
    }

    private static @Nullable String simpleQuoted(@NonNull String source, int start, int end) {
        if (end - start < 2) return null;

        char quote = source.charAt(start);
        if ((quote != '\'' && quote != '"') || source.charAt(end - 1) != quote) {
            return null;
        }

        for (int index = start + 1; index < end - 1; index++) {
            char value = source.charAt(index);
            if (value == '\\' || value == quote) {
                return null;
            }
        }

        return source.substring(start + 1, end - 1);
    }

    private void handleTag(@NonNull TagInfo info) {
        if (isFastColorTag(info.name) && resolver.has(info.name)) {
            if (info.closing) {
                closeTag(info);
            } else {
                handleModifyingTag(info);
            }

            return;
        }

        Tag tag = resolveTag(info);

        if (info.closing) {
            if (!resolver.has(info.name)) {
                literal(info);
                return;
            }

            if (tag == ParserDirective.RESET) return;

            closeTag(info);
            return;
        }

        if (tag == null) {
            literal(info);
            return;
        }

        if (tag instanceof Modifying) {
            if (handleModifyingTag(info)) return;
            unsupported = true;
            return;
        }

        if (tag == ParserDirective.RESET) {
            flushText();
            while (frames.size() > 1) closeTop();
            return;
        }

        if (tag instanceof Inserting inserting) {
            flushText();

            if (info.selfClosing || !inserting.allowsChildren()) {
                frames.peek().children.add(inserting.value());
            } else {
                frames.push(new Frame(info.name, info.arguments, inserting.value()));
            }

            return;
        }

        if (tag instanceof PreProcess preProcess) {
            flushText();
            parseSegment(preProcess.value(), 0, preProcess.value().length());
            return;
        }

        literal(info);
    }

    private void closeTag(@NonNull TagInfo info) {
        if (!resolver.has(info.name)) {
            literal(info);
            return;
        }

        Frame matching = findFrame(info);
        if (matching == null) {
            literal(info);
            return;
        }

        while (frames.peek() != matching) closeTop();
        closeTop();
    }

    private boolean isFastColorTag(@NonNull String name) {
        return name.equals("gradient")
                || name.equals("rainbow")
                || name.equals("transition")
                || name.equals("pride");
    }

    private boolean handleModifyingTag(@NonNull TagInfo info) {
        if (info.name.equals("transition")) {
            ColorList colors = parseColorList(info.arguments);
            if (colors == null) {
                literal(info);
                return true;
            }

            Tag styling = Tag.styling(transitionColor(colors.colors(), colors.phase()));
            if (!(styling instanceof Inserting inserting)) return false;

            flushText();
            if (info.selfClosing) {
                frames.peek().children.add(inserting.value());
            } else {
                frames.push(new Frame(info.name, info.arguments, inserting.value()));
            }

            return true;
        }

        Colorizer colorizer = switch (info.name) {
            case "gradient" -> createGradient(info.arguments);
            case "rainbow" -> createRainbow(info.arguments);
            case "pride" -> createPride(info.arguments);
            default -> null;
        };

        if (colorizer == null) {
            literal(info);
            return true;
        }

        if (!info.selfClosing) {
            flushText();
            frames.push(new Frame(info.name, info.arguments, colorizer));
        }

        return true;
    }

    private @Nullable Colorizer createGradient(@NonNull List<String> arguments) {
        ColorList colors = parseColorList(arguments);
        return colors == null ? null : new GradientColorizer(colors.colors(), colors.phase());
    }

    private @Nullable Colorizer createRainbow(@NonNull List<String> arguments) {
        boolean reversed = false;
        int phase = 0;

        if (!arguments.isEmpty()) {
            String value = arguments.getFirst();
            if (value.startsWith("!")) {
                reversed = true;
                value = value.substring(1);
            }

            if (!value.isEmpty()) {
                try {
                    phase = Integer.parseInt(value);
                } catch (NumberFormatException _) {
                    return null;
                }
            }
        }

        return new RainbowColorizer(reversed, phase);
    }

    private @Nullable Colorizer createPride(@NonNull List<String> arguments) {
        String flag = "pride";
        double phase = 0.0;

        if (!arguments.isEmpty()) {
            String first = arguments.getFirst().toLowerCase(java.util.Locale.ROOT);
            if (PrideColors.flag(first) != null) {
                flag = first;
                if (arguments.size() > 1) {
                    try {
                        phase = Double.parseDouble(arguments.get(1));
                    } catch (NumberFormatException _) {
                        return null;
                    }
                }
            } else if (!first.isEmpty()) {
                try {
                    phase = Double.parseDouble(first);
                } catch (NumberFormatException _) {
                    return null;
                }
            }
        }

        TextColor[] colors = PrideColors.flag(flag);
        return colors == null ? null : new GradientColorizer(colors, phase);
    }

    private @Nullable ColorList parseColorList(@NonNull List<String> arguments) {
        if (arguments.isEmpty()) {
            return new ColorList(PrideColors.DEFAULT_GRADIENT, 0.0);
        }

        List<TextColor> colors = new ArrayList<>(arguments.size());
        double phase = 0.0;

        for (int index = 0; index < arguments.size(); index++) {
            String value = arguments.get(index);
            TextColor color = parseColor(value);
            if (color != null) {
                colors.add(color);
                continue;
            }

            if (index == arguments.size() - 1) {
                try {
                    phase = Double.parseDouble(value);
                } catch (NumberFormatException _) {
                    return null;
                }
                break;
            }

            return null;
        }

        if (colors.size() == 1) return null;
        if (colors.isEmpty()) {
            return new ColorList(PrideColors.DEFAULT_GRADIENT, phase);
        }

        return new ColorList(colors.toArray(new TextColor[0]), phase);
    }

    private @Nullable TextColor parseColor(@NonNull String value) {
        TextColor cached = COLOR_CACHE.getIfPresent(value);
        if (cached != null) return cached;

        TextColor color = TextColor.fromHexString(value);
        if (color == null) {
            NamedTextColor named = NamedTextColor.NAMES.value(value);
            if (named != null) {
                color = named;
            } else if (value.equals("dark_grey")) {
                color = NamedTextColor.DARK_GRAY;
            } else if (value.equals("grey")) {
                color = NamedTextColor.GRAY;
            }
        }

        if (color != null) {
            COLOR_CACHE.put(value, color);
        }

        return color;
    }

    private @NonNull TextColor transitionColor(TextColor @NonNull [] colors, double phase) {
        float adjustedPhase = (float) phase;
        boolean reversed = false;
        TextColor[] values = colors;

        if (adjustedPhase < 0f) {
            reversed = true;
            adjustedPhase += 1f;
            values = new TextColor[colors.length];
            for (int i = 0; i < colors.length; i++) {
                values[i] = colors[colors.length - 1 - i];
            }
        }

        float steps = 1f / (values.length - 1);
        for (int index = 1; index < values.length; index++) {
            float point = index * steps;
            if (point >= adjustedPhase) {
                float factor = 1 + (adjustedPhase - point) * (values.length - 1);
                return reversed
                        ? TextColor.lerp(1 - factor, values[index], values[index - 1])
                        : TextColor.lerp(factor, values[index - 1], values[index]);
            }
        }

        return values[0];
    }

    private @Nullable Tag resolveTag(@NonNull TagInfo info) {
        if (!info.closing) {
            if (info.name.equals("click") && resolver.has(info.name)) {
                return createClickTag(info.arguments);
            }

            if (info.name.equals("hover") && resolver.has(info.name)) {
                Tag hover = createHoverTag(info.arguments);
                if (hover != null) return hover;
            }
        }

        return resolveSafely(info);
    }

    private @Nullable Tag createClickTag(@NonNull List<String> arguments) {
        if (arguments.size() < 2) return null;

        String action = asciiLowercase(arguments.getFirst());
        String value = argumentFactory.apply(arguments.get(1)).value();

        try {
            return switch (action) {
                case "open_url" -> Tag.styling(ClickEvent.openUrl(value));
                case "open_file" -> Tag.styling(ClickEvent.openFile(value));
                case "run_command" -> Tag.styling(ClickEvent.runCommand(value));
                case "suggest_command" -> Tag.styling(ClickEvent.suggestCommand(value));
                case "change_page" -> Tag.styling(ClickEvent.changePage(Integer.parseInt(value)));
                case "copy_to_clipboard" -> Tag.styling(ClickEvent.copyToClipboard(value));
                default -> null;
            };
        } catch (IllegalArgumentException _) {
            return null;
        }
    }

    private @Nullable Tag createHoverTag(@NonNull List<String> arguments) {
        if (arguments.size() < 2) return null;
        if (!arguments.getFirst().equalsIgnoreCase("show_text")) return null;

        String value = arguments.get(1);

        try {
            Component parsed = new FastParser(value, resolver, miniMessage, context, argumentFactory).parse();
            Component text = parsed == null ? miniMessage.deserialize(value, resolver) : FastMessageParser.compact(parsed);

            return Tag.styling(HoverEvent.showText(text));
        } catch (Exception _) {
            return null;
        }
    }

    private @Nullable Tag resolveSafely(@NonNull TagInfo info) {
        try {
            return resolver.resolve(info.name, new FastArgumentQueue(info.arguments, argumentFactory), context);
        } catch (ParsingException | ClassCastException _) {
            return null;
        }
    }

    private @Nullable Frame findFrame(@NonNull TagInfo info) {
        for (Frame frame : frames) {
            if (frame.name == null) return null;
            if (!frame.name.equals(info.name)) continue;
            if (closes(info.arguments, frame.arguments)) return frame;
        }

        return null;
    }

    private boolean closes(@NonNull List<String> closeArguments, @NonNull List<String> openArguments) {
        if (closeArguments.size() > openArguments.size()) return false;

        for (int i = 0; i < closeArguments.size(); i++) {
            if (!closeArguments.get(i).equals(openArguments.get(i))) return false;
        }

        return true;
    }

    private void flushText() {
        if (text.length() == 0) return;

        frames.peek().children.add(Component.text(text.toString()));
        text.setLength(0);
    }

    private void closeTop() {
        flushText();

        Frame frame = frames.pop();
        frames.peek().children.add(frame.build());
    }

    private static @NonNull String asciiLowercase(@NonNull String value) {
        int index = -1;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current >= 'A' && current <= 'Z') {
                index = i;
                break;
            }
        }

        if (index < 0) return value;

        char[] chars = value.toCharArray();
        for (int i = index; i < chars.length; i++) {
            char current = chars[i];
            if (current >= 'A' && current <= 'Z') {
                chars[i] = (char) (current + 32);
            }
        }

        return new String(chars);
    }

    private void literal(@NonNull TagInfo info) {
        text.append(info.source, info.start, info.end);
    }

    private static @NonNull Component combine(@NonNull List<Component> children) {
        if (children.isEmpty()) return Component.empty();
        if (children.size() == 1) return children.getFirst();

        return Component.text().append(children).build();
    }

    private record TagInfo(
            @NonNull String source,
            int start,
            int end,
            @NonNull String name,
            @NonNull List<String> arguments,
            boolean closing,
            boolean selfClosing
    ) {
    }

    private record ColorList(
            TextColor @NonNull [] colors,
            double phase
    ) {
    }

    private static final class Frame {

        private final @Nullable String name;
        private final @NonNull List<String> arguments;
        private final @NonNull Component base;
        private final @Nullable Colorizer colorizer;
        private final @NonNull List<Component> children = new ArrayList<>(4);

        private Frame(@Nullable String name, @NonNull List<String> arguments, @NonNull Component base) {
            this.name = name;
            this.arguments = arguments;
            this.base = base;
            this.colorizer = null;
        }

        private Frame(@Nullable String name, @NonNull List<String> arguments, @NonNull Colorizer colorizer) {
            this.name = name;
            this.arguments = arguments;
            this.base = Component.empty();
            this.colorizer = colorizer;
        }

        private @NonNull Component build() {
            if (colorizer != null) {
                int size = ColorTransform.sizeOf(children);
                if (size <= 0) return combine(children);

                colorizer.init(size);

                List<Component> colored = new ArrayList<>(children.size());
                for (int i = 0; i < children.size(); i++) {
                    colored.add(ColorTransform.apply(children.get(i), colorizer));
                }

                return combine(colored);
            }

            if (children.isEmpty()) return base;

            if (base instanceof TextComponent emptyBase
                    && emptyBase.content().isEmpty()
                    && emptyBase.children().isEmpty()) {
                Style style = emptyBase.style();

                if (children.size() == 1) {
                    Component child = children.getFirst();
                    if (child instanceof TextComponent text && text.children().isEmpty()) {
                        if (style.isEmpty()) return child;
                        return Component.text(text.content(),
                                text.style().merge(style, Style.Merge.Strategy.IF_ABSENT_ON_TARGET));
                    }
                } else {
                    boolean plain = true;
                    for (Component child : children) {
                        if (!(child instanceof TextComponent text
                                && text.children().isEmpty()
                                && text.style().isEmpty())) {
                            plain = false;
                            break;
                        }
                    }

                    if (plain) {
                        StringBuilder content = new StringBuilder();
                        for (Component child : children) {
                            content.append(((TextComponent) child).content());
                        }

                        return Component.text(content.toString(), style);
                    }
                }
            }

            List<Component> merged = new ArrayList<>(base.children().size() + children.size());
            merged.addAll(base.children());
            merged.addAll(children);
            return base.children(merged);
        }

    }

}
