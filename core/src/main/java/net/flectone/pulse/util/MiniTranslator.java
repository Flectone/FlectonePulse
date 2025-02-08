/*
    MIT License

    Copyright (c) 2022 Daniil Z. (idanix@list.ru)

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package net.flectone.pulse.util;

import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A "translator" from legacy minecraft formatting (e.g. &a &4 &l) to MiniMessage-acceptable format
 */
public final class MiniTranslator {
    private static final Set<Option> DEF_OPTIONS = Collections.unmodifiableSet(EnumSet.of(
            Option.COLOR, Option.FORMAT, Option.GRADIENT, Option.FAST_RESET
    ));

    private static final Pattern HEX_COLOR = Pattern.compile("[\\da-fA-F]{6}");
    private static final Pattern LEGACY_HEX_COLOR = Pattern.compile("&([\\da-fA-F])".repeat(6));

    private MiniTranslator() {}

    /**
     * Translate text to MiniMessage format with default options (everything but {@link Option#CLOSE_COLORS})
     * @param text text to translate
     * @return translated string
     */
    public static @NotNull String toMini(@NotNull String text) {
        return toMini(text, DEF_OPTIONS);
    }

    /**
     * Translate text to MiniMessage format
     * @param text text to translate
     * @param options options to use
     * @return translated string
     */
    public static @NotNull String toMini(@NotNull String text, @NotNull Option @NotNull ... options) {
        return toMini(text, EnumSet.copyOf(Arrays.asList(options)));
    }

    /**
     * Translate text to MiniMessage format
     * @param text text to translate
     * @param options options to use
     * @return translated string
     */
    public static @NotNull String toMini(@NotNull String text, @NotNull Collection<@NotNull Option> options) {
        List<String> order = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        boolean defCloseValue = options.contains(Option.CLOSE_COLORS);
        boolean fastReset = options.contains(Option.FAST_RESET);
        boolean closeLastColor = true;
        for (int index = 0; index < text.length(); index++) {
            char ch = text.charAt(index);
            if (ch != '&') {
                builder.append(ch);
            } else  {
                if (text.length() == ++index) {
                    builder.append('&');
                    break;
                }
                ch = text.charAt(index);
                String tag = tagByChar(ch, options);
                if (tag == null) {
                    builder.append('&').append(ch);
                    continue;
                }
                switch (tag) {
                    case "color" -> {
                        if (ch == '#') {
                            if (text.length() > index + 6) {
                                String color = text.substring(index + 1, index + 7);
                                if (HEX_COLOR.matcher(color).matches()) {
                                    handleClosing(order, builder, closeLastColor, fastReset);
                                    closeLastColor = defCloseValue;
                                    String builtTag = "color:#" + color;
                                    builder.append('<').append(builtTag).append('>');
                                    index += 6;
                                    order.add(builtTag);
                                    continue;
                                }
                            }
                        } else {
                            if (text.length() > index + 12) {
                                String color = text.substring(index + 1, index + 13);
                                Matcher colorMatcher = LEGACY_HEX_COLOR.matcher(color);
                                if (colorMatcher.matches()) {
                                    handleClosing(order, builder, closeLastColor, fastReset);
                                    closeLastColor = defCloseValue;
                                    String builtTag = "color:#" + colorMatcher.replaceAll("$1$2$3$4$5$6");
                                    builder.append('<').append(builtTag).append('>');
                                    index += 12;
                                    order.add(builtTag);
                                    continue;
                                }
                            }
                        }
                        builder.append('&').append(ch);
                    }
                    case "gradient" -> {
                        int endIndex = -1;
                        for (int inner = index + 1; inner < text.length(); inner++) {
                            char inCh = text.charAt(inner);
                            if (inCh == '&') {
                                endIndex = inner;
                                break;
                            } else if (!(('a' <= inCh && inCh <= 'z') || ('0' <= inCh && inCh <= '9') || inCh == '#' || inCh == '-')) {
                                break;
                            }
                        }
                        String[] split;
                        if (endIndex == -1 || (split = text.substring(index + 1, endIndex).split("-")).length == 1) {
                            builder.append("&@");
                            continue;
                        }
                        List<String> colors = new ArrayList<>(split.length);
                        for (String color : split) {
                            if (color.length() == 1) {
                                color = colorByChar(color.charAt(0));
                                if (color == null) break;
                            } else if (color.startsWith("#")) {
                                if (!HEX_COLOR.matcher(color.substring(1)).matches()) {
                                    break;
                                }
                            } else if (NamedTextColor.NAMES.value(color) == null) {
                                break;
                            }
                            colors.add(color);
                        }
                        if (colors.size() == split.length) {
                            index = endIndex;
                            handleClosing(order, builder, closeLastColor, fastReset);
                            closeLastColor = true;
                            builder.append("<gradient:").append(String.join(":", colors)).append('>');
                            order.add(tag);
                        }
                    }
                    case "reset" -> {
                        order.clear();
                        builder.append("<reset>");
                    }
                    case "b", "u", "st", "i", "obf" -> {
                        order.add(tag);
                        builder.append('<').append(tag).append('>');
                    }
                    default -> {
                        handleClosing(order, builder, closeLastColor, fastReset);
                        closeLastColor = defCloseValue;
                        order.add(tag);
                        builder.append('<').append(tag).append('>');
                    }
                }
            }
        }
        if (closeLastColor || !fastReset) handleClosing(order, builder, closeLastColor, closeLastColor && fastReset);
        return builder.toString();
    }

    private static void handleClosing(List<String> order, StringBuilder builder, boolean closeFirst, boolean fastReset) {
        if (fastReset && order.size() > 1) {
            builder.append("<reset>");
        } else for (int i = order.size(), until = closeFirst ? 0 : 1; i > until; i--) {
            builder.append("</").append(order.get(i - 1)).append('>');
        }
        order.clear();
    }

    private static @Nullable String tagByChar(char ch, Collection<Option> options) {
        if (('0' <= ch && ch <= '9') || ('a' <= ch && ch <= 'f') || ch == '#' || ch == 'x') {
            if (!options.contains(Option.COLOR)) return null;
            return switch (ch) {
                case 'x', '#' -> "color";
                default -> colorByChar(ch);
            };
        } else if (('k' <= ch && ch <= 'o') || ch == 'r') {
            if (!options.contains(Option.FORMAT)) return null;
            return switch (ch) {
                case 'r' -> "reset";
                case 'l' -> "b";
                case 'n' -> "u";
                case 'm' -> "st";
                case 'o' -> "i";
                case 'k' -> "obf";

                default -> null;
            };
        } else if (ch == '@' && options.contains(Option.GRADIENT)) {
            return "gradient";
        }
        return null;
    }

    private static @Nullable String colorByChar(char ch) {
        return switch (ch) {
            case '0' -> "black";
            case '1' -> "dark_blue";
            case '2' -> "dark_green";
            case '3' -> "dark_aqua";
            case '4' -> "dark_red";
            case '5' -> "dark_purple";
            case '6' -> "gold";
            case '7' -> "gray";
            case '8' -> "dark_gray";
            case '9' -> "blue";
            case 'a' -> "green";
            case 'b' -> "aqua";
            case 'c' -> "red";
            case 'd' -> "light_purple";
            case 'e' -> "yellow";
            case 'f' -> "white";

            default -> null;
        };
    }

    /**
     * Translation options
     */
    public enum Option {
        /**
         * Translate color (e.g. &a &1 #123456)
         */
        COLOR,
        /**
         * Translate formatting (e.g. &l &r)
         */
        FORMAT,
        /**
         * Translate custom gradient format (e.g. &@gold-#123456&)
         */
        GRADIENT,
        /**
         * Place the reset tag when there's 2+ tags to close is used
         */
        FAST_RESET,
        /**
         * Close color tags when another color was found
         */
        CLOSE_COLORS
    }
}
