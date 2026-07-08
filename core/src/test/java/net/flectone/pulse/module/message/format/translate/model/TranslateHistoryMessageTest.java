package net.flectone.pulse.module.message.format.translate.model;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Offline unit tests for {@link TranslateHistoryMessage#replaceMessageText}.
 *
 * <p>Pure function over Adventure components — no DI, no network, no Mockito.
 * KeybindComponent is used as the "non-text" node because it flattens to its
 * keybind string under {@link PlainTextComponentSerializer}
 * ({@code ComponentFlattener.basic()} maps KeybindComponent -> keybind), so it
 * deterministically contributes characters to the flattened plain text — exactly
 * like the Translatable player-head nodes the production code deals with.
 */
class TranslateHistoryMessageTest {

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    // Regression: the message lives entirely inside text nodes (the common chat case).
    // Replacement must swap exactly the matched literal and leave everything else intact.
    @Test
    void replacesLiteralWithinSingleTextNode() {
        Component root = Component.text("hello world");

        Component result = TranslateHistoryMessage.replaceMessageText(root, "world", "WORLD");

        assertEquals("hello WORLD", plain(result));
    }

    // Regression: a non-text node (player head) sits BEFORE the message. Its characters must
    // shift the match offset, but since it does NOT overlap the match window the counter is
    // left untouched and the match inside the following text node is replaced correctly.
    @Test
    void nonTextNodeBeforeMatchDoesNotBreakReplacement() {
        Component root = Component.empty()
                .append(Component.keybind("H"))          // non-text head, plain "H"
                .append(Component.text("hello world"));

        Component result = TranslateHistoryMessage.replaceMessageText(root, "world", "WORLD");

        assertEquals("Hhello WORLD", plain(result));
    }

    // Regression: match split across several sibling text nodes with different styles
    // (the private-message case matchLiteral cannot handle).
    @Test
    void replacesLiteralSpanningMultipleTextNodes() {
        Component root = Component.empty()
                .append(Component.text("hel"))
                .append(Component.text("lo wo"))
                .append(Component.text("rld"));

        Component result = TranslateHistoryMessage.replaceMessageText(root, "hello world", "buenos dias");

        assertEquals("buenos dias", plain(result));
    }

    // Bug #11 reproduction: the matched literal CROSSES a non-text node.
    // Tree flattens to "abXYcd"; matching "bXYc" spans text("ab") + keybind("XY") + text("cd").
    // Before the fix the keybind's two chars were not counted into matchedChars, so the counter
    // "made up" the shortfall from the trailing text node and dropped the extra 'd' too, yielding
    // "aRXY". After the fix the non-text node's overlapping chars are counted, so only the real
    // remainder ('c') is dropped and 'd' survives: "aRXYd" (the keybind stays verbatim because
    // we never edit non-text nodes).
    @Test
    void matchCrossingNonTextNodeDoesNotOverConsume() {
        Component root = Component.empty()
                .append(Component.text("ab"))
                .append(Component.keybind("XY"))         // non-text, plain "XY"
                .append(Component.text("cd"));

        Component result = TranslateHistoryMessage.replaceMessageText(root, "bXYc", "R");

        assertEquals("aRXYd", plain(result));
    }

    // Conservative behavior: the whole match lies INSIDE a non-text node we cannot rewrite.
    // Rather than corrupt the surrounding text, the string is left untouched (no replacement).
    @Test
    void matchFullyInsideNonTextNodeLeavesTextUntouched() {
        Component root = Component.empty()
                .append(Component.text("ab"))
                .append(Component.keybind("XY"))
                .append(Component.text("cd"));

        Component result = TranslateHistoryMessage.replaceMessageText(root, "XY", "R");

        assertEquals("abXYcd", plain(result));
    }
}
