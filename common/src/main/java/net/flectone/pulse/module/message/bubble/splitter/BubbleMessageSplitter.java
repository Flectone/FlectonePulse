package net.flectone.pulse.module.message.bubble.splitter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BubbleMessageSplitter {

    // a non-text component (head, sprite, ...) is one unbreakable visual unit
    private static final char OBJECT_MARKER = '\uFFFF';

    public @NonNull List<Component> split(@NonNull Component component,
                                          int maxLength,
                                          int hintBufferLength,
                                          @NonNull String wordBreakHint,
                                          int maxCount) {
        if (maxLength <= 0 || maxCount <= 0) return List.of();

        int measuredLength = measure(component);
        if (measuredLength == 0) return List.of();
        if (measuredLength <= maxLength) return List.of(component);

        List<Piece> pieces = new ArrayList<>();
        StringBuilder measure = new StringBuilder();
        collectPieces(component, Style.empty(), pieces, measure);
        if (pieces.isEmpty()) return List.of();

        int length = measure.length();
        if (length <= maxLength) return List.of(component);

        List<Component> chunks = new ArrayList<>(Math.min(maxCount, 4));
        int count = 0;
        int start = 0;
        int buffer = Math.max(hintBufferLength, 0);

        while (start < length && count < maxCount) {
            int end = safeEnd(measure, start, Math.min(start + maxLength, length), length);

            if (end == length) {
                int chunkStart = trim(measure, start, end, true);
                int chunkEnd = trim(measure, chunkStart, end, false);

                chunks.add(buildChunk(pieces, chunkStart, chunkEnd, ""));
                break;
            }

            int breakAt = findBreak(measure, start, end, buffer);
            if (breakAt != -1) {
                breakAt = safeBreak(measure, breakAt, start);
                if (breakAt == start) {
                    breakAt = -1;
                }
            }

            if (breakAt != -1) {
                int chunkStart = trim(measure, start, breakAt, true);
                int chunkEnd = trim(measure, chunkStart, breakAt, false);

                chunks.add(buildChunk(pieces, chunkStart, chunkEnd, ""));

                char breakChar = measure.charAt(breakAt);
                start = breakChar == OBJECT_MARKER || Character.isSurrogate(breakChar)
                        ? breakAt
                        : breakAt + 1;
            } else {
                chunks.add(buildChunk(pieces, start, end, wordBreakHint));
                start = end;
            }

            count++;
        }

        return chunks.stream().filter(this::isVisible).toList();
    }

    private int measure(@NonNull Component component) {
        int length = component instanceof TextComponent text ? text.content().length() : 1;

        for (Component child : component.children()) {
            length += measure(child);
        }

        return length;
    }

    private void collectPieces(@NonNull Component component,
                               @NonNull Style parentStyle,
                               @NonNull List<Piece> pieces,
                               @NonNull StringBuilder measure) {
        Style style = parentStyle.merge(component.style(), Style.Merge.Strategy.ALWAYS);

        if (component instanceof TextComponent text) {
            if (!text.content().isEmpty()) {
                String content = text.content();
                pieces.add(new Piece(Component.text(content, style), content, measure.length()));
                measure.append(content);
            }
        } else {
            Component object = component.children(List.of());
            if (!style.isEmpty()) {
                object = Component.empty().style(style).append(object);
            }

            pieces.add(new Piece(object, "", measure.length()));
            measure.append(OBJECT_MARKER);
        }

        for (Component child : component.children()) {
            collectPieces(child, style, pieces, measure);
        }
    }

    private @NonNull Component buildChunk(@NonNull List<Piece> pieces,
                                          int start,
                                          int end,
                                          @NonNull String suffix) {
        Component result = Component.empty();
        Style suffixStyle = Style.empty();

        for (Piece piece : pieces) {
            if (piece.start() >= end || piece.end() <= start) continue;

            if (piece.object()) {
                result = result.append(piece.component());
            } else {
                int from = Math.max(start, piece.start()) - piece.start();
                int to = Math.min(end, piece.end()) - piece.start();

                result = result.append(((TextComponent) piece.component()).content(piece.text().substring(from, to)));
            }

            suffixStyle = piece.component().style();
        }

        if (!suffix.isEmpty()) {
            result = result.append(Component.text(suffix, suffixStyle));
        }

        return result;
    }

    private int findBreak(@NonNull CharSequence measure, int start, int end, int buffer) {
        for (int index = end; index > start && index > end - buffer; index--) {
            if (isNotLetter(measure.charAt(index))) {
                return index;
            }
        }

        return -1;
    }

    // never cut a surrogate pair in half
    private int safeEnd(@NonNull CharSequence measure, int start, int end, int length) {
        if (end >= length) return length;
        if (end <= start || !isSurrogatePairAt(measure, end)) return end;

        return end - 1 > start ? end - 1 : end + 1;
    }

    private int safeBreak(@NonNull CharSequence measure, int index, int start) {
        if (index <= start || !isSurrogatePairAt(measure, index)) return index;

        return index - 1;
    }

    private boolean isSurrogatePairAt(@NonNull CharSequence measure, int index) {
        return index > 0
                && index < measure.length()
                && Character.isHighSurrogate(measure.charAt(index - 1))
                && Character.isLowSurrogate(measure.charAt(index));
    }

    private boolean isVisible(@NonNull Component component) {
        if (!(component instanceof TextComponent text)) return true;
        if (!text.content().isEmpty()) return true;

        for (Component child : component.children()) {
            if (isVisible(child)) return true;
        }

        return false;
    }

    private int trim(@NonNull CharSequence measure, int start, int end, boolean fromStart) {
        if (fromStart) {
            while (start < end && measure.charAt(start) <= ' ') {
                start++;
            }

            return start;
        }

        while (end > start && measure.charAt(end - 1) <= ' ') {
            end--;
        }

        return end;
    }

    private boolean isNotLetter(char symbol) {
        if (Character.isLetter(symbol)) return false;
        if (Character.isSpaceChar(symbol)) return true;

        return !Character.isDigit(symbol);
    }

    private record Piece(
            @NonNull Component component,
            @NonNull String text,
            int start
    ) {

        private int end() {
            return start + (text.isEmpty() ? 1 : text.length());
        }

        private boolean object() {
            return text.isEmpty();
        }

    }

}
