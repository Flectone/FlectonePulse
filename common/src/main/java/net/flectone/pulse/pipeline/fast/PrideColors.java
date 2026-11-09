package net.flectone.pulse.pipeline.fast;

import net.kyori.adventure.text.format.TextColor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

final class PrideColors {

    static final TextColor[] DEFAULT_GRADIENT = {
            TextColor.color(0xFFFFFF),
            TextColor.color(0x000000)
    };

    private static final Map<String, TextColor[]> FLAGS = Map.ofEntries(
            Map.entry("pride", colors(0xE50000, 0xFF8D00, 0xFFEE00, 0x028121, 0x004CFF, 0x770088)),
            Map.entry("progress", colors(0xFFFFFF, 0xFFAFC7, 0x73D7EE, 0x613915, 0x000000, 0xE50000, 0xFF8D00, 0xFFEE00, 0x028121, 0x004CFF, 0x770088)),
            Map.entry("trans", colors(0x5BCFFB, 0xF5ABB9, 0xFFFFFF, 0xF5ABB9, 0x5BCFFB)),
            Map.entry("bi", colors(0xD60270, 0x9B4F96, 0x0038A8)),
            Map.entry("pan", colors(0xFF1C8D, 0xFFD700, 0x1AB3FF)),
            Map.entry("nb", colors(0xFCF431, 0xFCFCFC, 0x9D59D2, 0x282828)),
            Map.entry("lesbian", colors(0xD62800, 0xFF9B56, 0xFFFFFF, 0xD462A6, 0xA40062)),
            Map.entry("ace", colors(0x000000, 0xA4A4A4, 0xFFFFFF, 0x810081)),
            Map.entry("agender", colors(0x000000, 0xBABABA, 0xFFFFFF, 0xBAF484, 0xFFFFFF, 0xBABABA, 0x000000)),
            Map.entry("demisexual", colors(0x000000, 0xFFFFFF, 0x6E0071, 0xD3D3D3)),
            Map.entry("genderqueer", colors(0xB57FDD, 0xFFFFFF, 0x49821E)),
            Map.entry("genderfluid", colors(0xFE76A2, 0xFFFFFF, 0xBF12D7, 0x000000, 0x303CBE)),
            Map.entry("intersex", colors(0xFFD800, 0x7902AA, 0xFFD800)),
            Map.entry("aro", colors(0x3BA740, 0xA8D47A, 0xFFFFFF, 0xABABAB, 0x000000)),
            Map.entry("femboy", colors(0xD260A5, 0xE4AFCD, 0xFEFEFE, 0x57CEF8, 0xFEFEFE, 0xE4AFCD, 0xD260A5)),
            Map.entry("baker", colors(0xCD66FF, 0xFF6599, 0xFE0000, 0xFE9900, 0xFFFF01, 0x009900, 0x0099CB, 0x350099, 0x990099)),
            Map.entry("philly", colors(0x000000, 0x784F17, 0xFE0000, 0xFD8C00, 0xFFE500, 0x119F0B, 0x0644B3, 0xC22EDC)),
            Map.entry("queer", colors(0x000000, 0x9AD9EA, 0x00A3E8, 0xB5E51D, 0xFFFFFF, 0xFFC90D, 0xFC6667, 0xFEAEC9, 0x000000)),
            Map.entry("gay", colors(0x078E70, 0x26CEAA, 0x98E8C1, 0xFFFFFF, 0x7BADE2, 0x5049CB, 0x3D1A78)),
            Map.entry("bigender", colors(0xC479A0, 0xECA6CB, 0xD5C7E8, 0xFFFFFF, 0xD5C7E8, 0x9AC7E8, 0x6C83CF)),
            Map.entry("demigender", colors(0x7F7F7F, 0xC3C3C3, 0xFBFF74, 0xFFFFFF, 0xFBFF74, 0xC3C3C3, 0x7F7F7F))
    );

    private PrideColors() {
    }

    static @Nullable TextColor[] flag(@NonNull String name) {
        return FLAGS.get(name);
    }

    private static TextColor @NonNull [] colors(int @NonNull ... values) {
        TextColor[] colors = new TextColor[values.length];
        for (int i = 0; i < values.length; i++) {
            colors[i] = TextColor.color(values[i]);
        }

        return colors;
    }

}
