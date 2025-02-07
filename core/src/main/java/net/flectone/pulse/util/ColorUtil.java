package net.flectone.pulse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import me.imdanix.text.MiniTranslator;
import net.flectone.pulse.logger.FLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Singleton
public class ColorUtil {

    private final int DEFAULT_ARGB = 0x40000000;

    private final Map<String, String> minecraftHexMap = new HashMap<>();
    private final Map<String, String> legacyHexMap = new HashMap<>();

    private final List<String> minecraftList = List.of(
            "black", "dark_blue", "dark_green",
            "dark_aqua", "dark_red", "dark_purple",
            "gold", "gray", "dark_gray",
            "blue", "green", "aqua",
            "red", "light_purple", "yellow",
            "white", "light_blue", "lime",
            "cyan", "magenta", "orange",
            "brown", "pink", "purple",
            "silver"
    );

    private final List<String> hexSymbolList = List.of(
            "0", "1", "2",
            "3", "4", "5",
            "6", "7", "8",
            "9", "a", "b",
            "c", "d", "e",
            "f"
    );

    private final FLogger fLogger;

    @Inject
    public ColorUtil(FLogger fLogger) {
        this.fLogger = fLogger;

        minecraftHexMap.put("black", "#000000");
        minecraftHexMap.put("dark_blue", "#0000AA");
        minecraftHexMap.put("dark_green", "#00AA00");
        minecraftHexMap.put("dark_aqua", "#00AAAA");
        minecraftHexMap.put("dark_red", "#AA0000");
        minecraftHexMap.put("dark_purple", "#AA00AA");
        minecraftHexMap.put("gold", "#FFAA00");
        minecraftHexMap.put("gray", "#AAAAAA");
        minecraftHexMap.put("dark_gray", "#555555");
        minecraftHexMap.put("blue", "#5555FF");
        minecraftHexMap.put("green", "#55FF55");
        minecraftHexMap.put("aqua", "#55FFFF");
        minecraftHexMap.put("red", "#FF5555");
        minecraftHexMap.put("light_purple", "#FF55FF");
        minecraftHexMap.put("yellow", "#FFFF55");
        minecraftHexMap.put("white", "#FFFFFF");
        minecraftHexMap.put("light_blue", "#55FFFF");
        minecraftHexMap.put("lime", "#55FF55");
        minecraftHexMap.put("cyan", "#55FFFF");
        minecraftHexMap.put("magenta", "#FF55FF");
        minecraftHexMap.put("orange", "#FFAA00");
        minecraftHexMap.put("brown", "#A52A2A");
        minecraftHexMap.put("pink", "#FFC0CB");
        minecraftHexMap.put("purple", "#800080");
        minecraftHexMap.put("silver", "#C0C0C0");

        legacyHexMap.put("&0", "#000000");
        legacyHexMap.put("&1", "#0000AA");
        legacyHexMap.put("&2", "#00AA00");
        legacyHexMap.put("&3", "#00AAAA");
        legacyHexMap.put("&4", "#AA0000");
        legacyHexMap.put("&5", "#AA00AA");
        legacyHexMap.put("&6", "#FFAA00");
        legacyHexMap.put("&7", "#AAAAAA");
        legacyHexMap.put("&8", "#555555");
        legacyHexMap.put("&9", "#5555FF");
        legacyHexMap.put("&a", "#55FF55");
        legacyHexMap.put("&b", "#55FFFF");
        legacyHexMap.put("&c", "#FF5555");
        legacyHexMap.put("&d", "#FF55FF");
        legacyHexMap.put("&e", "#FFFF55");
        legacyHexMap.put("&f", "#FFFFFF");
    }

    public String toMiniMessage(String message) {
        return MiniTranslator.toMini(message
                        .replace("§", "&")
                        .replaceAll("(?<!:)#", "&#")
                );
    }

    public int parseHexToArgb(String hex) {
        hex = hex.trim().replace("#", "");

        if (hex.length() != 3 && hex.length() != 4 && hex.length() != 6 && hex.length() != 8) {
            fLogger.warning("Incorrect HEX string length");
            return DEFAULT_ARGB;
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
            return DEFAULT_ARGB;
        }
    }

}
