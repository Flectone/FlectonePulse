package net.flectone.pulse.converter;

import net.kyori.adventure.text.format.NamedTextColor;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Recognizes and converts the color formats accepted in configs, namely legacy codes,
 * named colors and hex values.
 * @author TheFaser
 */
public interface ColorConverter {

    /**
     * The background used when a hex color cannot be parsed.
     */
    int DEFAULT_BACKGROUND_COLOR = 0x40000000;

    /**
     * Every legacy color and decoration code the plugin recognizes.
     */
    List<String> LEGACY_COLORS = List.of(
            "&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7", "&8", "&9",
            "&a", "&b", "&c", "&d", "&e", "&f", "&k", "&l", "&m", "&n", "&o"
    );

    /**
     * Every named adventure color, written as the MiniMessage tag that produces it.
     */
    List<String> NAMED_COLORS = NamedTextColor.NAMES.values()
            .stream()
            .map(namedTextColor -> "<" + namedTextColor.toString() + ">")
            .toList();

    /**
     * Checks that a color is one this plugin understands.
     *
     * @param color the color to check
     * @return the normalized color, or null if it is not recognized
     */
    @Nullable String isCorrect(String color);

    /**
     * Converts a hex color to a packed ARGB integer.
     *
     * @param hex the hex color, with or without a leading hash
     * @return the packed ARGB value, or {@link #DEFAULT_BACKGROUND_COLOR} if the text is not valid hex
     */
    int parseHexToArgb(String hex);

}
