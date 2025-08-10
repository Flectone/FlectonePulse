package net.flectone.pulse.processing.converter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Singleton
public class ColorConverter {

    private final int defaultBackground = 0x40000000;

    private final List<String> legacyColors = List.of(
            "&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7", "&8", "&9",
            "&a", "&b", "&c", "&d", "&e", "&f", "&k", "&l", "&m", "&n", "&o"
    );

    private final List<String> namedColors = NamedTextColor.NAMES.values()
            .stream()
            .map(namedTextColor -> "<" + namedTextColor.toString() + ">")
            .toList();

    private final FLogger fLogger;

    @Inject
    public ColorConverter(FLogger fLogger) {
        this.fLogger = fLogger;
    }

    @Nullable
    public String isCorrect(String color) {
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

        if (legacyColors.contains(color)) {
            return color;
        }

        if (namedColors.contains(color)) {
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

    public int parseHexToArgb(String hex) {
        hex = hex.trim().replace("#", "");

        if (hex.length() != 3 && hex.length() != 4 && hex.length() != 6 && hex.length() != 8) {
            fLogger.warning("Incorrect HEX string length");
            return defaultBackground;
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
        } catch (NumberFormatException e) {
            fLogger.warning("Incorrect HEX characters");
            return defaultBackground;
        }
    }

}
