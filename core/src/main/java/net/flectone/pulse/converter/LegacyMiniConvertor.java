package net.flectone.pulse.converter;

/*
    MIT License

    Copyright (c) 2022-2025 Daniil Z. (idanix@list.ru)

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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

/**
 * A "translator" from legacy minecraft formatting (e.g. &a &4 &l) to MiniMessage-acceptable format
 */

@Singleton
public final class LegacyMiniConvertor implements MessageProcessor {

    private final Set<Option> DEF_OPTIONS = Collections.unmodifiableSet(EnumSet.of(
            Option.COLOR,
            Option.HEX_COLOR_STANDALONE,
            Option.COLOR_DOUBLE_HASH,
            Option.FORMAT,
            Option.GRADIENT,
            Option.FAST_RESET,
            Option.RESET,
            Option.DOUBLE_TO_ESCAPE
    ));

    private final Pattern HEX_COLOR = Pattern.compile("[\\da-fA-F]{6}");
    private final Permission.Message.Format formatPermission;

    private final PermissionChecker permissionChecker;

    @Inject
    public LegacyMiniConvertor(FileResolver fileResolver,
                               MessageProcessRegistry messageProcessRegistry,
                               PermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;

        formatPermission = fileResolver.getPermission().getMessage().getFormat();

        messageProcessRegistry.register(200, this);
    }

    @Override
    public void process(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (!messageContext.isColors()) return;
        if (messageContext.isUserMessage() && !permissionChecker.check(sender, formatPermission.getAll())) return;

        String message = toMini(messageContext.getMessage());
        messageContext.setMessage(message);
    }

    /**
     * Translate text to MiniMessage format with default options (everything but {@link Option#CLOSE_COLORS})
     * @param text text to translate
     * @return translated string
     */
    public @NotNull String toMini(@NotNull String text) {
        return toMini(text, DEF_OPTIONS);
    }

    /**
     * Translate text to MiniMessage format
     * @param text text to translate
     * @param options options to use
     * @return translated string
     */
    public @NotNull String toMini(@NotNull String text, @NotNull Collection<@NotNull Option> options) {
        text = text.replace('§', '&');
        if (options.contains(Option.DOUBLE_TO_ESCAPE)) {
            text = text.replace("&&", "§");
        }

        if (options.contains(Option.COLOR_DOUBLE_HASH)) {
            text = replaceDoubleHashHexColor(text);
        }

        if (options.contains(Option.HEX_COLOR_STANDALONE)) {
            text = replaceHexColorStandalone(text);
        }

        final String colorTagStart = options.contains(Option.VERBOSE_HEX_COLOR) ? "color:#" : "#";

        List<String> order = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        boolean defCloseValue = options.contains(Option.CLOSE_COLORS);
        boolean fastReset = options.contains(Option.FAST_RESET);
        boolean closeLastTag = defCloseValue;

        for (
                int index = 0, nextIndex = text.indexOf('&'), length = text.length();
                index < length;
                index++, nextIndex = text.indexOf('&', index)
        ) {
            if (nextIndex == -1) {
                builder.append(text, index, length);
                break;
            }

            builder.append(text, index, nextIndex);
            index = nextIndex + 1;

            if (index >= length) {
                builder.append('&');
                break;
            }

            char symbol = text.charAt(index);
            String tag = tagByChar(symbol, options);
            if (tag == null) {
                builder.append('&').append(symbol);
                continue;
            }

            switch (tag) {
                case "hex_color" -> {
                    if (symbol == '#') {
                        if (length > index + 6 && isHexPattern(text, index + 1)) {
                            handleClosing(order, builder, closeLastTag, fastReset);
                            closeLastTag = defCloseValue;
                            String builtTag = colorTagStart + text.substring(index + 1, index + 7);
                            builder.append('<').append(builtTag).append('>');
                            index += 6;
                            order.add(builtTag);
                            continue;
                        }
                    } else if (length > index + 12) {
                        String color = extractLegacyHex(text, index + 1);
                        if (color != null) {
                            handleClosing(order, builder, closeLastTag, fastReset);
                            closeLastTag = defCloseValue;
                            String builtTag = colorTagStart + color;
                            builder.append('<').append(builtTag).append('>');
                            index += 12;
                            order.add(builtTag);
                            continue;
                        }
                    }
                    builder.append('&').append(symbol);
                }
                case "gradient" -> {
                    int endIndex = -1;
                    for (int inner = index + 1; inner < length; inner++) {
                        char inCh = Character.toLowerCase(text.charAt(inner));
                        if (inCh == '@') {
                            endIndex = inner;
                            break;
                        } else if (!(
                                ('a' <= inCh && inCh <= 'z') ||
                                        ('0' <= inCh && inCh <= '9') ||
                                        inCh == '#' || inCh == '-'
                        )) {
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
                        } else if (color.startsWith("#") && (color.length() < 7 || !isHexPattern(color, 1))) {
                            break;
                        } else if (NamedTextColor.NAMES.value(color) == null) {
                            break;
                        }
                        colors.add(color);
                    }
                    if (colors.size() == split.length) {
                        index = endIndex;
                        handleClosing(order, builder, closeLastTag, fastReset);
                        closeLastTag = true;
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
                    handleClosing(order, builder, closeLastTag, fastReset);
                    closeLastTag = defCloseValue;
                    order.add(tag);
                    builder.append('<').append(tag).append('>');
                }
            }
        }
        if (closeLastTag || !fastReset) {
            handleClosing(order, builder, closeLastTag, closeLastTag && fastReset);
        }
        return builder.toString().replace('§', '&');
    }

    private @Nullable String extractLegacyHex(String input, int from) {
        StringBuilder builder = new StringBuilder(6);
        for (int i = from + 1, end = from + 12; i <= end; i += 2) {
            char ch = input.charAt(i);
            if (!isHexDigit(ch)) {
                return null;
            }
            builder.append(ch);
        }
        return builder.toString();
    }

    private String replaceHexColorStandalone(String text) {
        StringBuilder result = new StringBuilder();
        int index = 0;

        while (index < text.length()) {
            int nextIndex = text.indexOf('#', index);
            if (nextIndex == -1) {
                result.append(text, index, text.length());
                break;
            }

            if (isHexColorStandalone(text, nextIndex)) {
                result.append(text, index, nextIndex).append("<color:").append(text, nextIndex, nextIndex + 7).append('>');
                index = nextIndex + 7;
            } else {
                result.append(text, index, nextIndex + 1);
                index = nextIndex + 1;
            }
        }

        return result.toString();
    }

    private String replaceDoubleHashHexColor(String text) {
        StringBuilder result = new StringBuilder();
        int index = 0;

        while (index < text.length()) {
            int startIndex = text.indexOf("<##", index);
            if (startIndex == -1) {
                result.append(text.substring(index));
                break;
            }

            result.append(text, index, startIndex);

            if (startIndex + 9 <= text.length() && text.charAt(startIndex + 9) == '>') {
                String hexColor = text.substring(startIndex + 3, startIndex + 9);
                if (HEX_COLOR.matcher(hexColor).matches()) {
                    result.append("<#").append(hexColor).append(">");
                    index = startIndex + 10;
                } else {
                    result.append("<##").append(hexColor).append(">");
                    index = startIndex + 10;
                }
            } else {
                result.append("<##");
                index = startIndex + 3;
            }
        }

        return result.toString();
    }

    private boolean isHexColorStandalone(String text, int index) {
        if (index + 6 >= text.length()) return false;

        char prevChar = index == 0
                ? ' '
                : text.charAt(index - 1);
        char nextChar = index + 7 >= text.length()
                ? ' '
                : text.charAt(index + 7);

        if (prevChar == '&') return false; // &#123456
        if (prevChar == '<' && nextChar == '>') return false; // <#123456>
        if (prevChar == ':' && (nextChar == '>' || nextChar == ':')) return false; // <color:#123456> | <gradient:#123456:#654321>

        return isHexPattern(text, index + 1);
    }

    private void handleClosing(List<String> order, StringBuilder builder, boolean closeLast, boolean fastReset) {
        if (fastReset && order.size() > 1) {
            builder.append("<reset>");
        } else for (int i = order.size() - 1, until = closeLast ? 0 : 1; i >= until; i--) {
            builder.append("</").append(order.get(i)).append('>');
        }
        order.clear();
    }

    private @Nullable String tagByChar(char ch, Collection<Option> options) {
        if (isHexDigit(ch)) {
            if (!options.contains(Option.COLOR)) return null;
            return colorByChar(ch);
        } else if (isHexPrefix(ch)) {
            if (!options.contains(Option.COLOR)) return null;
            return "hex_color";
        } else if (isFormatChar(ch)) {
            if (!options.contains(Option.FORMAT)) return null;
            return switch (ch) {
                case 'k', 'K' -> "obf";
                case 'l', 'L' -> "b";
                case 'm', 'M' -> "st";
                case 'n', 'N' -> "u";
                case 'o', 'O' -> "i";
                default -> throw new IllegalStateException("Provided impossible format symbol '" + ch + "'");
            };
        } else if (ch == 'r' || ch == 'R') {
            if (!options.contains(Option.RESET)) return null;
            return "reset";
        } else if (ch == '@') {
            if (!options.contains(Option.GRADIENT)) return null;
            return "gradient";
        }
        return null;
    }

    private @Nullable String colorByChar(char ch) {
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
            case 'a', 'A' -> "green";
            case 'b', 'B' -> "aqua";
            case 'c', 'C' -> "red";
            case 'd', 'D' -> "light_purple";
            case 'e', 'E' -> "yellow";
            case 'f', 'F' -> "white";

            default -> null;
        };
    }

    private boolean isHexPattern(String str, int from) {
        for (int index = from, end = from + 6; index < end; index++) {
            if (!isHexDigit(str.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    private boolean isHexDigit(char ch) {
        return switch (ch) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                 'a', 'b', 'c', 'd', 'e', 'f',
                 'A', 'B', 'C', 'D', 'E', 'F' -> true;
            default -> false;
        };
    }

    private boolean isHexPrefix(char ch) {
        return switch (ch) {
            case '#', 'x', 'X' -> true;
            default -> false;
        };
    }

    private boolean isFormatChar(char ch) {
        return switch (ch) {
            case 'k', 'l', 'm', 'n', 'o',
                 'K', 'L', 'M', 'N', 'O' -> true;
            default -> false;
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
         * Translate standalone hex colors (e.g. #123456)
         */
        HEX_COLOR_STANDALONE,
        /**
         * Translate double hash color (e.g. <##123456>)
         */
        COLOR_DOUBLE_HASH,
        /**
         * Use the full MiniMessage color format {@code <color:#123456>} instead of the shortened one {@code <#123456>}
         */
        VERBOSE_HEX_COLOR,
        /**
         * Translate formatting (e.g. &l &r)
         */
        FORMAT,
        /**
         * Translate custom gradient format (e.g. &@gold-#123456&)
         */
        GRADIENT,
        /**
         * Place the reset tag when there's 2+ tags to close
         */
        FAST_RESET,
        /**
         * Translate the reset tag {@code &r}
         */
        RESET,
        /**
         * Close color tags when another color was found
         */
        CLOSE_COLORS,
        /**
         * Allow EssentialsX-like {@code &&} escaping
         */
        DOUBLE_TO_ESCAPE
    }
}