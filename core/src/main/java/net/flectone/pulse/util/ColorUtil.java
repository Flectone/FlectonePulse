package net.flectone.pulse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Singleton
public class ColorUtil {

    private final Map<String, String> legacyAdventureMap = new HashMap<>();
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

    @Inject
    public ColorUtil() {
        legacyAdventureMap.put("&0", "<black>");
        legacyAdventureMap.put("&1", "<dark_blue>");
        legacyAdventureMap.put("&2", "<dark_green>");
        legacyAdventureMap.put("&3", "<dark_aqua>");
        legacyAdventureMap.put("&4", "<dark_red>");
        legacyAdventureMap.put("&5", "<dark_purple>");
        legacyAdventureMap.put("&6", "<gold>");
        legacyAdventureMap.put("&7", "<gray>");
        legacyAdventureMap.put("&8", "<dark_gray>");
        legacyAdventureMap.put("&9", "<blue>");
        legacyAdventureMap.put("&a", "<green>");
        legacyAdventureMap.put("&A", "<green>");
        legacyAdventureMap.put("&b", "<aqua>");
        legacyAdventureMap.put("&B", "<aqua>");
        legacyAdventureMap.put("&c", "<red>");
        legacyAdventureMap.put("&C", "<red>");
        legacyAdventureMap.put("&d", "<light_purple>");
        legacyAdventureMap.put("&D", "<light_purple>");
        legacyAdventureMap.put("&e", "<yellow>");
        legacyAdventureMap.put("&E", "<yellow>");
        legacyAdventureMap.put("&f", "<white>");
        legacyAdventureMap.put("&F", "<white>");
        legacyAdventureMap.put("&k", "<obf>");
        legacyAdventureMap.put("&K", "<obf>");
        legacyAdventureMap.put("&l", "<b>");
        legacyAdventureMap.put("&L", "<b>");
        legacyAdventureMap.put("&m", "<st>");
        legacyAdventureMap.put("&M", "<st>");
        legacyAdventureMap.put("&n", "<u>");
        legacyAdventureMap.put("&N", "<u>");
        legacyAdventureMap.put("&o", "<i>");
        legacyAdventureMap.put("&O", "<i>");
        legacyAdventureMap.put("&r", "<reset>");
        legacyAdventureMap.put("&R", "<reset>");

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

    public String convertColorsToAdventure(String message) {
        for (Map.Entry<String, String> entry : legacyAdventureMap.entrySet()) {
            message = message
                    .replace(entry.getKey(), entry.getValue())
                    .replace(entry.getKey().replace("&", "§"), entry.getValue())
                    .replace("&#", "#")
                    .replace("§#", "#");
        }

        return message.replaceAll("(?<!:)#([a-fA-F0-9]{6})", "<color:#$1>");
    }

}
