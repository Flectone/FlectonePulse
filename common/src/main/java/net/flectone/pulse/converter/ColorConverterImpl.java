package net.flectone.pulse.converter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.logging.FLogger;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;

@Getter
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ColorConverterImpl implements ColorConverter {

    private final FLogger fLogger;

    @Override
    public @Nullable String isCorrect(String color) {
        if (color == null) return null;

        if (isHex(color)) {
            return color;
        }

        if (color.startsWith("<gradient:#") && color.endsWith(">") && color.length() == 26) {
            String[] colorParts = color.split(":");
            if (colorParts.length == 3 && isHex(colorParts[1]) && isHex(colorParts[2].substring(0, 7))) {
                return color;
            }
        }

        if (LEGACY_COLORS.contains(color)) {
            return color;
        }

        if (NAMED_COLORS.contains(color)) {
            return color;
        }

        return null;
    }

    private boolean isHex(String color) {
        if (color.length() != 7 || !color.startsWith("#")) {
            return false;
        }

        for (int i = 1; i < color.length(); i++) {
            char c = color.charAt(i);
            int digit = Character.digit(c, 16);
            if (digit == -1) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int parseHexToArgb(String hex) {
        hex = Strings.CS.replace(hex.trim(), "#", "");

        if (hex.length() != 3 && hex.length() != 4 && hex.length() != 6 && hex.length() != 8) {
            fLogger.warning("Incorrect HEX string length");
            return DEFAULT_BACKGROUND_COLOR;
        }

        // #RGB -> RRGGBB, #RGBA -> RRGGBBAA
        if (hex.length() == 3 || hex.length() == 4) {
            StringBuilder stringBuilder = new StringBuilder();
            for (char value : hex.toCharArray()) {
                stringBuilder.append(value).append(value);
            }

            hex = stringBuilder.toString();
        }

        int alpha = 0xFF;
        int rgbPartLength = hex.length();

        if (hex.length() == 8) {
            alpha = Integer.parseInt(hex.substring(6, 8), 16);
            rgbPartLength = 6;
        }

        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, rgbPartLength), 16);

            return (alpha << 24) | (r << 16) | (g << 8) | b;
        } catch (NumberFormatException _) {
            fLogger.warning("Incorrect HEX characters");
            return DEFAULT_BACKGROUND_COLOR;
        }
    }

}
