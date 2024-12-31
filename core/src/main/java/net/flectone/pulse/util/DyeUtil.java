package net.flectone.pulse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Getter
@Singleton
public class DyeUtil {

    private final Map<String, TextColor> DYE_HEX_MAP = new HashMap<>();

    @Inject
    public DyeUtil() {
        DYE_HEX_MAP.put("WHITE_DYE", TextColor.fromHexString("#ffffff"));
        DYE_HEX_MAP.put("GRAY_DYE", TextColor.fromHexString("#999999"));
        DYE_HEX_MAP.put("LIGHT_GRAY_DYE", TextColor.fromHexString("#cccccc"));
        DYE_HEX_MAP.put("BLACK_DYE", TextColor.fromHexString("#333333"));
        DYE_HEX_MAP.put("RED_DYE", TextColor.fromHexString("#ff3333"));
        DYE_HEX_MAP.put("ORANGE_DYE", TextColor.fromHexString("#ff9900"));
        DYE_HEX_MAP.put("YELLOW_DYE", TextColor.fromHexString("#ffff00"));
        DYE_HEX_MAP.put("LIME_DYE", TextColor.fromHexString("#33ff33"));
        DYE_HEX_MAP.put("GREEN_DYE", TextColor.fromHexString("#009900"));
        DYE_HEX_MAP.put("LIGHT_BLUE_DYE", TextColor.fromHexString("#99ccff"));
        DYE_HEX_MAP.put("CYAN_DYE", TextColor.fromHexString("#33cccc"));
        DYE_HEX_MAP.put("BLUE_DYE", TextColor.fromHexString("#3366ff"));
        DYE_HEX_MAP.put("PURPLE_DYE", TextColor.fromHexString("#9900cc"));
        DYE_HEX_MAP.put("MAGENTA_DYE", TextColor.fromHexString("#ff66ff"));
        DYE_HEX_MAP.put("PINK_DYE", TextColor.fromHexString("#ff99cc"));
        DYE_HEX_MAP.put("BROWN_DYE", TextColor.fromHexString("#cc6600"));
    }

    public TextColor dyeToHex(String name) {
        return DYE_HEX_MAP.get(name.toUpperCase());
    }

    @Nullable
    public String hexToDye(@NotNull TextColor textColor) {
        return DYE_HEX_MAP.entrySet().stream()
                .filter(entry -> entry.getValue().equals(textColor))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
